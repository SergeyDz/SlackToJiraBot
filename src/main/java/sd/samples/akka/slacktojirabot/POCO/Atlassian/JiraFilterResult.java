/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Atlassian;

import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class JiraFilterResult extends JiraIssuesContainer {
    
    public JiraFilterResult(List<Issue> issues) {
        super(issues);
    }
    
}
