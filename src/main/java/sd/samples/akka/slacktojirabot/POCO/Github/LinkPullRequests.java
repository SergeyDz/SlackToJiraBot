/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Github;

import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class LinkPullRequests {
    
    private final List<Issue> issues; 
    
    public final boolean HasShowChangeLog;  
    
    public LinkPullRequests(List<Issue> issues, boolean hasShowChangeLog)
    {
        this.issues = issues;
        this.HasShowChangeLog = hasShowChangeLog;
    }
    
    public List<Issue> getIssues()
    {
        return this.issues;
    }            
}
