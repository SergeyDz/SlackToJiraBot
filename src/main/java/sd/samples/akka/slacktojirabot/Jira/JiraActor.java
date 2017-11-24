/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.joda.time.DateTime;
import sd.samples.akka.slacktojirabot.Mapping.Attachment.JiraIssuesToAttachmentFormatter;
import sd.samples.akka.slacktojirabot.Mapping.Message.JiraIssuesResultFormatter;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraFilterResult;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintRequest;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintResult;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendAttachment;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.samples.akka.slacktojirabot.POCO.Slack.SlackUserRequest;
import sd.samples.akka.slacktojirabot.Slack.NotFoundMessage;

/**
 *
 * @author sdzyuban
 */
public class JiraActor extends UntypedActor {

    private final ActorRef senderActor;
    private final BotConfigurationInfo config;
    private final SlackUserRequest slackRequest;
    private final DateTime ShowItemsModifiedOn;
    
    public JiraActor(SlackUserRequest request, ActorRef senderActor, DateTime showItemsModifiedOn, BotConfigurationInfo config)
    {
        this.senderActor = senderActor;
        this.config = config;
        this.slackRequest = request;
        this.ShowItemsModifiedOn = showItemsModifiedOn;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        
        if(message instanceof JiraRequest)
        {
            JiraRequest request = (JiraRequest)message;
            ActorSelection sprintActor = context().actorSelection("akka://bot-system/user/BotEngineActor/JiraAgileActor");
            sprintActor.tell(new JiraSprintRequest(slackRequest.TeamName), self());
        }
        else if(message instanceof JiraSprintResult)
        {
            JiraSprintResult jiraSprintResult = (JiraSprintResult)message;
            ActorRef jiraFilterActor = context().actorOf(Props.create(JiraFilterActor.class, this.slackRequest.HasShowChangeLog, this.ShowItemsModifiedOn, this.config));
            jiraFilterActor.tell(jiraSprintResult, self());
        }
        else if(message instanceof JiraFilterResult)
        {
            JiraFilterResult result = (JiraFilterResult)message;
            
            if(result.Issues != null && !result.Issues.isEmpty())
            {
                senderActor.tell(new SendMessage(new JiraIssuesResultFormatter(result.Issues, this.ShowItemsModifiedOn, config).call()), self());
            }
            else if(this.ShowItemsModifiedOn == null)
            {
                senderActor.tell(new SendMessage("Hobbits not found"), self());
            }
        }
        else if(message instanceof NotFoundMessage)
        {
            NotFoundMessage notFound = (NotFoundMessage)message;
            senderActor.tell(new SendAttachment(notFound.Message), senderActor);
        }
    }
    
}
