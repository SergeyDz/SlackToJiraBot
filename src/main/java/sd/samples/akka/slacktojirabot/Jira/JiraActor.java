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
import sd.samples.akka.slacktojirabot.GitHub.GitHubPullRequestActor;
import sd.samples.akka.slacktojirabot.Mapping.Attachment.JiraIssuesToAttachmentFormatter;
import sd.samples.akka.slacktojirabot.Mapping.Message.JiraIssuesResultFormatter;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraFilterResult;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraIssuesContainer;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintRequest;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintResult;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Github.LinkPullRequests;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.samples.akka.slacktojirabot.POCO.Slack.SlackUserRequest;

/**
 *
 * @author sdzyuban
 */
public class JiraActor extends UntypedActor {

    private final ActorRef senderActor;
    private final BotConfigurationInfo config;
    private final SlackUserRequest slackRequest;
    
    public JiraActor(SlackUserRequest request, ActorRef senderActor, BotConfigurationInfo config)
    {
        this.senderActor = senderActor;
        this.config = config;
        this.slackRequest = request;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        
        if(message instanceof JiraRequest)
        {
            JiraRequest request = (JiraRequest)message;
            ActorSelection sprintActor = context().actorSelection("akka://bot-system/user/JiraAgileActor");
            sprintActor.tell(new JiraSprintRequest(slackRequest.TeamName), self());
        }
        if(message instanceof JiraSprintResult)
        {
            JiraSprintResult jiraSprintResult = (JiraSprintResult)message;
            ActorRef jiraFilterActor = context().actorOf(Props.create(JiraFilterActor.class, this.slackRequest.HasShowChangeLog, this.config));
            jiraFilterActor.tell(jiraSprintResult, self());
        }
        if(message instanceof JiraFilterResult)
        {
            JiraFilterResult jiraFilterResult = (JiraFilterResult)message;
            ActorSelection gitHubActor = context().actorSelection("akka://bot-system/user/GitHubActor");
            gitHubActor.tell(new LinkPullRequests(jiraFilterResult.Issues, this.slackRequest.HasShowChangeLog), self());
        }
        if(message instanceof LinkPullRequests)
        {
            LinkPullRequests result = (LinkPullRequests)message;
            if(config.HasUseSlackAttachment)
            {
                senderActor.tell(new JiraIssuesToAttachmentFormatter(result.getIssues() , config).call(), null);
            }
            else
            {
                senderActor.tell(new SendMessage(new JiraIssuesResultFormatter(result.getIssues(), config).call()), null);
            }
        }
    }
    
}
