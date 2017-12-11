/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Slack.Listeners;

import sd.bot.akka.slacktojirabot.Slack.Senders.SlackChannelMessageSenderActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;
import sd.bot.akka.slacktojirabot.Jira.JiraSchedullerActor;
import sd.bot.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.bot.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;
import sd.bot.akka.slacktojirabot.Slack.WhereAmILocator;

/**
 *
 * @author sdzyuban
 */
public class SlackStatusWatchActor extends UntypedActor {

    protected final BotConfigurationInfo config;
    protected final String channel;
    
    private SlackSession session;
    
    public SlackStatusWatchActor(BotConfigurationInfo config, String channel) throws IOException
    {
        this.config = config;
        this.channel = channel;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        
        if(message instanceof String && "start".equals(message))
        {     
            System.out.println("Creting Actor<SlackStatusUpdateActor> for channel " + this.channel);
            String team = new WhereAmILocator("bot status", this.channel).call();
            
            JiraRequest request = new JiraRequest(team, true);
            
            ActorRef jiraSchedullerActor = context().actorOf(Props.create(JiraSchedullerActor.class, request, self(), this.config), "JiraSchedullerActor-" + this.channel);
            Cancellable cancellable = context().system().scheduler().schedule(Duration.Zero(),
            Duration.create(this.config.WatchSprintChangesTimeout, TimeUnit.MINUTES), jiraSchedullerActor, request, context().system().dispatcher(), self());
        } 
        else if(message instanceof SendMessage)
        {
            System.out.println("DEPRECATED. Status message will be send !!!");
            
            SendMessage jiraStatusUpdate = (SendMessage)message;
            session = SlackSessionFactory.createWebSocketSlackSession(this.config.SlackAuthorizationKey);
            session.connect();

            SlackChannel theChannel = session.findChannelByName(this.channel);
            SlackConnectionInfo connection = new SlackConnectionInfo(session, theChannel);
            
            ActorRef channelSenderActor = context().actorOf(Props.create(SlackChannelMessageSenderActor.class, connection, this.config), "ChannelSenderActor-" + this.channel);
            channelSenderActor.tell(jiraStatusUpdate, null);
            channelSenderActor.tell(PoisonPill.getInstance(), null);
        }
    }
}
