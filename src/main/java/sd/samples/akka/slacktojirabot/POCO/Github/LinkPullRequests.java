/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Github;

import com.ullink.slack.simpleslackapi.SlackUser;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import java.util.List;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraIssuesContainer;

/**
 *
 * @author sdzyuban
 */
public class LinkPullRequests {
    
    private final JiraIssuesContainer issues; 
    
    public final boolean HasShowChangeLog;  
    
    public LinkPullRequests(JiraIssuesContainer issues, boolean hasShowChangeLog)
    {
        this.issues = issues;
        this.HasShowChangeLog = hasShowChangeLog;
    }
    
    public List<Issue> getIssues()
    {
        return this.issues.Issues;
    }
    
    public SlackUser getsender()
    {
        return this.issues.Sender;
    }
            
}
