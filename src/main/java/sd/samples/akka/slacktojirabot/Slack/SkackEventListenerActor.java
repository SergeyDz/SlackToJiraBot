/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Slack;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import sd.samples.akka.slacktojirabot.GitHub.GitHubPullRequestActor;
import sd.samples.akka.slacktojirabot.Jira.JiraFilterActor;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.JiraFilterRequest;
import sd.samples.akka.slacktojirabot.POCO.SendMessage;
import sd.samples.akka.slacktojirabot.POCO.SlackConnectionInfo;

/**
 *
 * @author sdzyuban
 */
public class SkackEventListenerActor extends UntypedActor {

    private final BotConfigurationInfo config;
    private final String channel;
    
    private ActorRef senderActor;
    private ActorRef jiraActor;
    private ActorRef gitActor;
    
    public SkackEventListenerActor(BotConfigurationInfo config, String channel)
    {
        this.config = config;
        this.channel = channel;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof String && "start".equals(message))
        {
            SlackSession session = SlackSessionFactory.createWebSocketSlackSession(this.config.SlackAuthorizationKey);
            session.connect();
            
            SlackChannel theChannel = session.findChannelByName(this.channel);
            senderActor = context().actorOf(Props.create(SlackMessageSenderActor.class, new SlackConnectionInfo(session, theChannel), this.config));
            gitActor = context().actorOf(Props.create(GitHubPullRequestActor.class, config));
            jiraActor = context().actorOf(Props.create(JiraFilterActor.class, senderActor, gitActor, config));
           
            
            registeringAListener(session, theChannel);
            
            // delete after implement
            gitActor.tell("start", null);
            
            System.out.println("Connection success");
        } 
        else if(message instanceof SendMessage)
        {
            senderActor.tell(message, null);
        }
    }
    
    public void registeringAListener(SlackSession session, SlackChannel theChannel) 
    {
        // first define the listener
        SlackMessagePostedListener messagePostedListener = (SlackMessagePosted event, SlackSession s) -> {
            if (!theChannel.getId().equals(event.getChannel().getId())) {
                return; 
            }
            String messageContent = event.getMessageContent();
            SlackUser messageSender = event.getSender();
            
            if(messageContent.startsWith("jirabot sprint"))
            {
                String team = new WhereAmILocator(messageContent, theChannel.getName()).call();

                if(team.isEmpty())
                {
                     senderActor.tell(new SendMessage("Sorry, but I can't find your team name. Please try _jirabot sprint jets_."), null);
                }
                else
                {
                    boolean hasShowChangeLog = messageContent.contains("status");
                    senderActor.tell(new SendMessage("Team found - " + team + "\nRequesting sprint info from Jira. Please wait :clock9:"), null);
                    jiraActor.tell(new JiraFilterRequest("", team, hasShowChangeLog), null);
                }
            } 
            else if(messageContent.equals("jirabot"))
            {
                String commands = String.format("Commands: \n%s \n%s \n%s \n%s", 
                        "_jirabot sprint_", 
                        "_jirabot sprint *team*_",
                        "_jirabot sprint status_",
                        "_jirabot sprint *team*_ status");
                senderActor.tell(new SendMessage(commands), null);
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
