/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Slack;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import java.io.IOException;
import sd.samples.akka.slacktojirabot.Jira.JiraActor;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.samples.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;

/**
 *
 * @author sdzyuban
 */
public class SkackEventListenerActor extends UntypedActor {

    private final BotConfigurationInfo config;
    private final String channel;
    private final RoundRobinPool pool = new RoundRobinPool(8);
    
    private SlackSession session;
    
    public SkackEventListenerActor(BotConfigurationInfo config, String channel) throws IOException
    {
        this.config = config;
        this.channel = channel;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof String && "start".equals(message))
        {
            session = SlackSessionFactory.createWebSocketSlackSession(this.config.SlackAuthorizationKey);
            session.connect();

            SlackChannel theChannel = session.findChannelByName(this.channel);
            SlackConnectionInfo connection = new SlackConnectionInfo(session, theChannel);
            
            ActorRef channelSenderActor = context().actorOf(Props.create(SlackChannelMessageSenderActor.class, connection, this.config));
            channelSenderActor.tell(new SendMessage(String.format("Connected %s. (DevOps Team support added !)", this.channel)), null);

            registeringAListener(connection, channelSenderActor);
            System.out.println("Connection success");
        } 
    }
    
    public void registeringAListener(SlackConnectionInfo connection, ActorRef senderActor) 
    {
        // first define the listener
        SlackMessagePostedListener messagePostedListener = (SlackMessagePosted event, SlackSession s) -> {
            if (!connection.Channel.getId().equals(event.getChannel().getId())) {
                return; 
            }
            String messageContent = event.getMessageContent().toLowerCase();
            SlackUser sender = event.getSender();
            
            if(messageContent.equals("jirabot"))
            {
                String commands = String.format("Commands: \n%s \n%s \n%s \n%s", 
                        "_jirabot_", 
                        "_jirabot *team*_",
                        "_jirabot status_",
                        "_jirabot *team*_ status");
                senderActor.tell(new SendMessage(commands), null);
            }
            else if(messageContent.startsWith("jirabot"))
            {
                String team = new WhereAmILocator(messageContent, connection.Channel.getName()).call();

                if(team.isEmpty())
                {
                     senderActor.tell(new SendMessage("Sorry, but I can't find your team name. Please try _jirabot sprint jets_."), null);
                }
                else
                {
                    boolean hasShowChangeLog = messageContent.contains("status");
                    senderActor.tell(new SendMessage("Team found - " + team + ". Please wait for private response.:clock9:"), null);
                    JiraRequest request = new JiraRequest(team, hasShowChangeLog);
                    ActorRef privateMessageSender = context().actorOf(pool.props(Props.create(SlackUserMessageSenderActor.class, connection, this.config, sender)));
                    ActorRef jiraActor = context().actorOf(pool.props(Props.create(JiraActor.class, request, privateMessageSender, this.config)));
                    jiraActor.tell(request, null);
                }
            } 
            else if(messageContent.startsWith("jirabot"))
            {
                senderActor.tell(new SendMessage("Hi, I don't understand. :scream:"), null);
            }
            else
            {
                System.out.println("Found message: " + messageContent);
            }
        };
        
        session.addMessagePostedListener(messagePostedListener);
    }
    
}
