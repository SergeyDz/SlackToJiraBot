/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;

/**
 *
 * @author sdzyuban
 */
public class JiraFilterMessage {
    
    public JiraFilterMessage(JiraSprint sprint, boolean hasShowChangeLog)
    {
        this.Sprint = sprint;
        this.HasShowChangeLog = hasShowChangeLog;
    }
    
    public final JiraSprint Sprint; 
    
    public final boolean HasShowChangeLog;  
}
