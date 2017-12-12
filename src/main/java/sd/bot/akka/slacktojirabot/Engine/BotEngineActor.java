/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Engine;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import scala.concurrent.Future;
import akka.dispatch.OnSuccess;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import sd.bot.akka.slacktojirabot.Jira.JiraSprintActor;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;
import sd.bot.akka.slacktojirabot.Slack.Listeners.Resolvers.SlackChannelListenerResolver;
import sd.bot.akka.slacktojirabot.Slack.Listeners.Resolvers.SlackJiraStatusWatchResolver;

/**
 *
 * @author sergey.d
 */
public class BotEngineActor extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof EngineConfiguration)
        {
            
            BotConfigurationInfo config = ((EngineConfiguration) message).GetBotConfiguration();
            ActorSystem system = context().system();
            
            ActorRef jiraAgileActor = context().actorOf(Props.create(JiraSprintActor.class, config), "JiraAgileActor");
               
            List<Future<ActorRef>> actors = new ArrayList<>();
            config.Channels.stream()
                    .forEach(channelName -> {
                        try
                        {
                            SlackSession session = SlackSessionFactory.createWebSocketSlackSession(config.SlackAuthorizationKey);
                            session.connect();

                            SlackChannel theChannel = session.findChannelByName(channelName);
                            SlackConnectionInfo connection = new SlackConnectionInfo(session, theChannel);
                            
                            actors.add(Futures.future(new SlackChannelListenerResolver(system, config, channelName, connection, session), system.dispatcher()));
                            actors.add(Futures.future(new SlackJiraStatusWatchResolver(system, config, channelName, connection, session), system.dispatcher()));
                        }
                        catch(Exception ex)
                        {
                            System.err.println(ex);
                        }
                    });
            
            Future<Iterable<ActorRef>> result = Futures.sequence(actors, system.dispatcher());

            result.onSuccess(new OnSuccess<Iterable<ActorRef>>() {

                @Override
                public void onSuccess(Iterable<ActorRef> success) throws Throwable {
                    StreamSupport.stream(success.spliterator(), false)
                            .forEach(actor -> {
                                System.out.println("Starting: " + actor.path().name());
                                actor.tell("start", null);
                            });
                }
            }, system.dispatcher());
        }
        else if(message instanceof BotEngineShutdown)
        {
            
        }
    }
    
}
