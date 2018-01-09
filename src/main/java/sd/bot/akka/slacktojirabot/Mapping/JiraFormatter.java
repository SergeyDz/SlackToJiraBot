/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Mapping;

import sd.bot.akka.slacktojirabot.POCO.Atlassian.Issue;

/**
 *
 * @author sdzyuban
 */
public class JiraFormatter {
    // Todo: make dynamic loinking with dictionary from Jira.
    public static String GetStatusTextById(String status)
    {
        String result = status;
               
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
            case "10000": 
                result = "In Dev";
                break;
            case "10001": 
                result = "In Testing";
                break;
            case "10002": 
                result = "New";
                break;
            case "10004": 
                result = "Ready For Testing";
                break;
            case "10003": 
                result = "QA Approved";
                break;
            case "10101": 
                result = "Waiting for info";
                break;
            case "10007": 
                result = "Done";
                break;
            case "13203": 
                result = "Acceptance";
                break;  
            case "10401": 
                result = "Inbox";
                break;
            case "10500": 
                result = "Analysis";
                break;
        }
        
        return result;
    }
    
    public static String GetStatusEmoji(String status)
    {
        return "`" + status + "`";
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
            case "Dev Sub-Task": 
                result = ":sub-task:";
                break;
            case "Sub-Bug": 
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
