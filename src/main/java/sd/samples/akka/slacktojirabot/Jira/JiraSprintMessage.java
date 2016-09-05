/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

/**
 *
 * @author sdzyuban
 */
public class JiraSprintMessage {
    
    public JiraSprintMessage(String teamName, boolean hasShowChangeLog)
    {
        this.TeamName = teamName;
        this.HasShowChangeLog = hasShowChangeLog;
    }
    
    public final boolean HasShowChangeLog; 
    
    public final String TeamName;
}
