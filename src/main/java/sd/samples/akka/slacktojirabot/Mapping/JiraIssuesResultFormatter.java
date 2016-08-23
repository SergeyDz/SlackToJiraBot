/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Issue;

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
            String line = String.format("%s %s %s %s - <%s|%s> - %s %s \n", 
                getStatusEmoji(issue.Status),
                getUserPic(issue.Assignee),
                getIssueType(issue.IssueType),
                getPullRequests(issue),
                issue.Url, 
                issue.Key, 
                issue.Summary,
                issue.StoryPoints > 0 ? String.format("_(%s sp)_", issue.StoryPoints) : "");
            
             builder.append(line);
        });
        
        return builder.toString();
    }
    
    private String getStatusEmoji(String status)
    {
        String result = "";
        
        switch(status)
        {
            case "Open": 
                result = ":umbrella_with_rain_drops:";
                break;
            case "In Progress": 
                result = ":pick:";
                break;
            case "Resolved": 
                result = ":partly_sunny:";
                break;
            case "Closed": 
                result = ":sunny:";
                break;
        }
        
        return result;
    }
    
    private String getIssueType(String type)
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
            default: 
                result = type;
                break;
        }
        
        return result;
    }
    
    private String getUserPic(String user)
    {
        return String.format("<https://%s/secure/ViewProfile.jspa?name=%s|:%s:>", config.JiraBaseUrl, user, user.replace(".", "_"));
    }
    
    private String getPullRequests(Issue issue)
    {
        String result = "";
        
        if(issue != null && issue.IsPullRequest != null && issue.IsPullRequest)
        {
            result = "<" + issue.PullRequestUrl + "|:github:>";
        }
        
        return result;
    }
}
