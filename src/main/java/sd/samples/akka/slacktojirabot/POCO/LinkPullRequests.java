/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO;

import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class LinkPullRequests {
    
    private List<Issue> issues; 
    
    public LinkPullRequests(List<Issue> issues)
    {
        this.issues = issues;
    }
    
    public List<Issue> getIssues()
    {
        return this.issues;
    }
}
