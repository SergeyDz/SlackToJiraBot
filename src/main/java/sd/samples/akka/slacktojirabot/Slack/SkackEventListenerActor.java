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
import java.io.IOException;
import sd.samples.akka.slacktojirabot.GitHub.GitHubPullRequestActor;
import sd.samples.akka.slacktojirabot.Jira.JiraFilterActor;
import sd.samples.akka.slacktojirabot.Jira.JiraFilterMessage;
import sd.samples.akka.slacktojirabot.Jira.JiraSprintActor;
import sd.samples.akka.slacktojirabot.Jira.JiraSprintMessage;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraFilterRequest;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.samples.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;

/**
 *
 * @author sdzyuban
 */
public class SkackEventListenerActor extends UntypedActor {

    private final BotConfigurationInfo config;
    private final String channel;
    
    private ActorRef senderActor;
    private ActorRef jiraActor;
    private final ActorRef gitActor;
    private final ActorRef jiraAgileActor;
    
    private SlackSession session;
    
    public SkackEventListenerActor(BotConfigurationInfo config, String channel) throws IOException
    {
        this.config = config;
        this.channel = channel;

        gitActor = context().actorOf(Props.create(GitHubPullRequestActor.class, config));
        jiraAgileActor = context().actorOf(Props.create(JiraSprintActor.class, config));
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof String && "start".equals(message))
        {
            session = SlackSessionFactory.createWebSocketSlackSession(this.config.SlackAuthorizationKey);
            session.connect();

            SlackChannel theChannel = session.findChannelByName(this.channel);
            senderActor = context().actorOf(Props.create(SlackMessageSenderActor.class, new SlackConnectionInfo(session, theChannel), this.config));
            jiraActor = context().actorOf(Props.create(JiraFilterActor.class, senderActor, gitActor, config));

            registeringAListener(session, theChannel);
            
            System.out.println("Connection success");
        } 
        else if(message instanceof SendMessage)
        {
            senderActor.tell(message, null);
        }
        else if(message instanceof JiraFilterMessage)
        {
            JiraFilterMessage filter = (JiraFilterMessage)message;
            jiraActor.tell(new JiraFilterMessage(filter.Sprint, filter.HasShowChangeLog), null);
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
                    senderActor.tell(new SendMessage(":robot_face: Team found - " + team + "\n:robot_face: Requesting sprint info from Jira. Please wait :clock9:"), null);
                    jiraAgileActor.tell(new JiraSprintMessage(team, hasShowChangeLog), self());
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
