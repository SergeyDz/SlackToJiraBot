/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Slack.Listeners;

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
import sd.bot.akka.slacktojirabot.Jira.JiraActor;
import sd.bot.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.bot.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;
import sd.bot.akka.slacktojirabot.Slack.Senders.SlackChannelMessageSenderActor;
import sd.bot.akka.slacktojirabot.Slack.Senders.SlackUserMessageSenderActor;
import sd.bot.akka.slacktojirabot.Slack.WhereAmILocator;

/**
 *
 * @author sdzyuban
 */
public class SlackEventListenerActor extends UntypedActor {

    protected final BotConfigurationInfo config;
    protected final String channel;
    protected final RoundRobinPool pool = new RoundRobinPool(8);
    
    private SlackSession session;
    SlackConnectionInfo connection = null;
    
    public SlackEventListenerActor(BotConfigurationInfo config, String channel) throws IOException
    {
        this.config = config;
        this.channel = channel;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println("Creting Actor<SlackEventListenerActor> for channel " + this.channel);
        if(message instanceof String && "start".equals(message))
        {
            System.out.println("SlackEventListenerActor is starting");
            session = SlackSessionFactory.createWebSocketSlackSession(this.config.SlackAuthorizationKey);
            session.connect();

            SlackChannel theChannel = session.findChannelByName(this.channel);
            connection = new SlackConnectionInfo(session, theChannel);
            
            ActorRef channelSenderActor = context().actorOf(Props.create(SlackChannelMessageSenderActor.class, connection, this.config), "ChannelSenderActor-" + this.channel);
            channelSenderActor.tell(new SendMessage(String.format("Connected %s.", this.channel)), null);

            registeringAListener(connection, channelSenderActor);
            System.out.println("Connection success");
        } 
    }
    
    protected void registeringAListener(SlackConnectionInfo connection, ActorRef senderActor) 
    {
        // first define the listener
        SlackMessagePostedListener messagePostedListener = (SlackMessagePosted event, SlackSession s) -> {
            if (!connection.Channel.getId().equals(event.getChannel().getId())) {
                return; 
            }
            String messageContent = event.getMessageContent().toLowerCase();
            SlackUser sender = event.getSender();
            
            if(messageContent.equals("bot"))
            {
                String commands = String.format("Commands: \n%s \n%s \n%s \n%s", 
                        "_bot_", 
                        "_bot *team*_",
                        "_bot status_",
                        "_bot *team*_ status");
                senderActor.tell(new SendMessage(commands), null);
            }
            else if(messageContent.startsWith("bot"))
            {
                String team = new WhereAmILocator(messageContent, connection.Channel.getName()).call();

                if(team.isEmpty())
                {
                     senderActor.tell(new SendMessage("Sorry, but I can't find your team name. Please try _bot sprint devops_."), null);
                }
                else
                {
                    boolean hasShowChangeLog = messageContent.contains("status");
                    senderActor.tell(new SendMessage("Team found - " + team + ". Please wait for private response.:clock9:"), null);
                    JiraRequest request = new JiraRequest(team, hasShowChangeLog);
                    ActorRef privateMessageSender = context().actorOf(pool.props(Props.create(SlackUserMessageSenderActor.class, connection, sender, this.config)));
                    ActorRef jiraActor = context().actorOf(Props.create(JiraActor.class, request, null, privateMessageSender, this.config));
                    jiraActor.tell(request, self());
                }
            } 
            else if(messageContent.startsWith("bot"))
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
