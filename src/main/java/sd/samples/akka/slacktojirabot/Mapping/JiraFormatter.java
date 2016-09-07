/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.Github.Status;

/**
 *
 * @author sdzyuban
 */
public class JiraFormatter {
    // Todo: make dynamic loinking with dictionary from Jira.
    public static String GetStatusTextById(String status)
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
    
    public static String GetStatusEmoji(String status)
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
    
    public static String GetIssueType(String type)
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
    
    public static String GetUserPic(String user)
    {
        if(user != null)
        {
        
            return String.format(":%s:", user.toLowerCase().replace(".", "_"));
        }else
        {
            return ":unassigned:";
        }
    }
    
    public static String GetPullRequests(Issue issue)
    {
        StringBuilder result = new StringBuilder();
        
        if(issue != null && issue.PullRequests != null && issue.PullRequests.size() > 0)
        {
            issue.PullRequests.forEach(a -> {
                if(a.Statuses != null && !a.Statuses.isEmpty())
                {
                    Status status = a.Statuses.stream().findFirst().get();
                    
                    if(null != status.Name)
                    switch (status.Name) {
                        case "success":
                            result.append("<").append(a.Url).append("|").append(":github_mergable:").append(">");
                            result.append("<").append(status.Url).append("|").append(":jenkins_build_success:").append(">");
                            break;
                        case "pending":
                            result.append("<").append(a.Url).append("|").append(":github_build:").append(">");
                            result.append("<").append(status.Url).append("|").append(":jenkins_building:").append(">");
                            break;
                        case "failed":
                            result.append("<").append(a.Url).append("|").append(":github_failed:").append(">");
                            result.append("<").append(status.Url).append("|").append(":jenkins_build_failed:").append(">");
                            break;
                        default:
                            result.append("<").append(a.Url).append("|").append(":github:").append(">");
                            break;
                    }
                }
                else
                {
                    result.append("<").append(a.Url).append("|").append(":github:").append(">");
                }
            });
        }
        
        return result.toString();
    }
    
     public static String GetFlags(Issue issue) {
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
