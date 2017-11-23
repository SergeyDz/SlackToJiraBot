/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping.Message;

import java.util.List;
import java.util.concurrent.Callable;
import sd.samples.akka.slacktojirabot.Mapping.JiraFormatter;
import sd.samples.akka.slacktojirabot.Mapping.JiraStatisticsFormatter;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;

/**
 *
 * @author sdzyuban
 */
public class JiraIssuesResultFormatter implements Callable<String> {

    private final List<Issue> issues;
    private final BotConfigurationInfo config;
    
    public  JiraIssuesResultFormatter(List<Issue> issues, BotConfigurationInfo config)
    {
        this.issues = issues;
        this.config = config;
    }
    
    @Override
    public String call() throws Exception {
        StringBuilder builder = new StringBuilder();
        
        builder.append(new JiraStatisticsFormatter(this.issues).call());
        
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
