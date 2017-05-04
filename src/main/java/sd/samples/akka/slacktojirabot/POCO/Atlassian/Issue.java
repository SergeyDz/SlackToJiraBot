/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Atlassian;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class Issue {
    
    public Issue()
    {
        this.Changelog = new ArrayList<>();
    }
    
    public String Status; 
    
    public String Assignee;
    
    public String IssueType;
    
    public String Key;
    
    public String Url;
    
    public String Summary;
    
    public Double StoryPoints;
    
    public List<JiraChangelogItem> Changelog;

    public List<String> Flagged;
}
