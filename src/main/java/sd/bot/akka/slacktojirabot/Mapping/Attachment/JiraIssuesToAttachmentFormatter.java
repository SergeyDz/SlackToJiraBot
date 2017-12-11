/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Mapping.Attachment;

import java.util.ArrayList;
import sd.bot.akka.slacktojirabot.Mapping.Message.JiraChangelogFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import sd.bot.akka.slacktojirabot.Mapping.JiraFormatter;
import sd.bot.akka.slacktojirabot.Mapping.JiraStatisticsFormatter;
import sd.bot.akka.slacktojirabot.POCO.Slack.Attachment;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.bot.akka.slacktojirabot.POCO.Slack.SendAttachment;

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
        SendAttachment attachments = new SendAttachment("");
        
        attachments.Message = new JiraStatisticsFormatter(this.issues).call();
        attachments.Attachments = new ArrayList<>();
        
        issues.forEach((issue) -> {
            Attachment attachment = new Attachment();
            
            attachment.Message = "\n";
            attachment.Message += JiraFormatter.GetFlags(issue);
            attachment.Message += String.format("%s %s %s - <%s|%s> - %s %s", 
                JiraFormatter.GetStatusEmoji(issue.Status),
                JiraFormatter.GetUserPic(issue.Assignee),
                JiraFormatter.GetIssueType(issue.IssueType),
                issue.Url, 
                issue.Key, 
                issue.Summary,
                issue.StoryPoints > 0 ? String.format("_(%s sp)_", issue.StoryPoints) : "");
            
            attachment.ChangelogItems = new JiraChangelogFormatter(issue, this.config).call();
            
            attachments.Attachments.add(attachment);
            
        });
        
        return attachments;
    }
}
