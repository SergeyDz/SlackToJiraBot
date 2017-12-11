/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Mapping.Message;

import java.util.List;
import java.util.concurrent.Callable;
import org.joda.time.DateTime;
import sd.bot.akka.slacktojirabot.Mapping.JiraFormatter;
import sd.bot.akka.slacktojirabot.Mapping.JiraStatisticsFormatter;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Atlassian.Issue;

/**
 *
 * @author sdzyuban
 */
public class JiraIssuesResultFormatter implements Callable<String> {

    private final List<Issue> issues;
    private final BotConfigurationInfo config;
    private final DateTime ShowItemsModifiedOn;
    
    public  JiraIssuesResultFormatter(List<Issue> issues, DateTime showItemsModifiedOn, BotConfigurationInfo config)
    {
        this.issues = issues;
        this.config = config;
        this.ShowItemsModifiedOn = showItemsModifiedOn;
    }
    
    @Override
    public String call() throws Exception {
        StringBuilder builder = new StringBuilder();
        
        if(this.ShowItemsModifiedOn == null)
        {
            builder.append(new JiraStatisticsFormatter(this.issues).call());
        }
        
        issues.forEach((issue) -> {
            String line = String.format("\n%s %s %s - <%s|%s> - *%s* %s", 
                JiraFormatter.GetStatusEmoji(issue.Status),
                JiraFormatter.GetUserPic(issue.Assignee),
                JiraFormatter.GetIssueType(issue.IssueType),
                issue.Url, 
                issue.Key, 
                issue.Summary,
                issue.StoryPoints > 0 ? String.format("_(%s sp)_", issue.StoryPoints) : "");
             
            builder.append(line);
            builder.append(new JiraChangelogFormatter(issue, this.config).call());
        });
        
        return builder.toString();
    }
}
