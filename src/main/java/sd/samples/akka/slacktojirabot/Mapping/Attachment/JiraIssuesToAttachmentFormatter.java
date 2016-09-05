/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping.Attachment;

import java.util.ArrayList;
import sd.samples.akka.slacktojirabot.Mapping.Message.JiraChangelogFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import sd.samples.akka.slacktojirabot.Mapping.JiraStatisticsFormatter;
import sd.samples.akka.slacktojirabot.POCO.Slack.Attachment;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendAttachment;

/**
 *
 * @author sdzyuban
 */
public class JiraIssuesToAttachmentFormatter implements Callable<SendAttachment> {

    private final List<Issue> issues;
    private final BotConfigurationInfo config;
    
    public  JiraIssuesToAttachmentFormatter(List<Issue> issues, BotConfigurationInfo config)
    {
        this.issues = issues;
        this.config = config;
    }
    
    @Override
    public SendAttachment call() throws Exception {
        SendAttachment attachments = new SendAttachment();
        
        attachments.Header = new JiraStatisticsFormatter(this.issues).call();
        attachments.Attachments = new ArrayList<>();
        
        issues.forEach((issue) -> {
            Attachment attachment = new Attachment();
            
            attachment.Message = "\n";
            attachment.Message += getFlags(issue);
            attachment.Message += String.format("%s %s %s %s - <%s|%s> - %s %s", 
                getStatusEmoji(issue.Status),
                getUserPic(issue.Assignee),
                getIssueType(issue.IssueType),
                getPullRequests(issue),
                issue.Url, 
                issue.Key, 
                issue.Summary,
                issue.StoryPoints > 0 ? String.format("_(%s sp)_", issue.StoryPoints) : "");
            
            attachment.ChangelogItems = new JiraChangelogFormatter(issue.Changelog, this.config).call();
            
            attachments.Attachments.add(attachment);
            
        });
        
        return attachments;
    }
    
    // Todo: make dynamic loinking with dictionary from Jira.
    public static String getStatusTextById(String status)
    {
        String result = "";
        
        switch(status)
        {
            case "1": 
                result = "Open";
                break;
            case "3": 
                result = "In Progress";
                break;
            case "5": 
                result = "Resolved";
                break;
            case "6": 
                result = "Closed";
                break;
             case "4": 
                result = "Reopened";
                break;
        }
        
        return result;
    }
    
    public static String getStatusEmoji(String status)
    {
        String result = "";
        
        switch(status)
        {
            case "Open": 
                result = ":open:";
                break;
            case "In Progress": 
                result = ":inprogress:";
                break;
            case "Resolved": 
                result = ":resolved:";
                break;
            case "Closed": 
                result = ":closed:";
                break;
             case "Reopened": 
                result = ":reopened:";
                break;
             default:
                 result = ":grey_question:";
                 break;
        }
        
        return result;
    }
    
    public static String getIssueType(String type)
    {
        String result = "";
        
        switch(type)
        {
            case "Story": 
                result = ":jira_story:";
                break;
            case "Spike": 
                result = ":jira_spike:";
                break;
            case "Task": 
                result = ":jira_task:";
                break;
            case "Bug": 
                result = ":jira_bug:";
                break;
            case "Improvement": 
                result = ":jira_improvement:";
                break;
            case "Epic": 
                result = ":jira_epic:";
                break;
            case "Sub-task": 
                result = ":sub-task:";
                break;
            case "Sub-bug": 
                result = ":sub-bug:";
                break;
            default: 
                result = type;
                break;
        }
        
        return result;
    }
    
    public static String getUserPic(String user)
    {
        return String.format(":%s:", user.toLowerCase().replace(".", "_"));
    }
    
    public static String getPullRequests(Issue issue)
    {
        String result = "";
        
        if(issue != null && issue.IsPullRequest != null && issue.IsPullRequest)
        {
            result = "<" + issue.PullRequestUrl + "|:github:>";
        }
        
        return result;
    }

    private String getFlags(Issue issue) {
        StringBuilder builder = new StringBuilder();
        
        if(issue.Flagged != null && !issue.Flagged.isEmpty() && issue.Flagged.stream().anyMatch(a -> a.contains("Definition")))
        {
            builder.append(":exclamation: ");
        }
        
        if(issue.Flagged != null && !issue.Flagged.isEmpty() && issue.Flagged.stream().anyMatch(a -> a.contains("Question")))
        {
            builder.append(":question: ");
        }
        
        return builder.toString();
    }
}
