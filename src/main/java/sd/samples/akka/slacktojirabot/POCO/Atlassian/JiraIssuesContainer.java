/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Atlassian;

import com.ullink.slack.simpleslackapi.SlackUser;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class JiraIssuesContainer {
    
    public JiraIssuesContainer(List<Issue> issues, SlackUser sernder)
    {
        this.Issues = issues;
        this.Sender = sernder;
    }
    
    public List<Issue> Issues;
    
    public SlackUser Sender;
}
