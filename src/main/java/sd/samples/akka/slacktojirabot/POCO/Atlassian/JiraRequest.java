/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Atlassian;

/**
 *
 * @author sdzyuban
 */
public class JiraRequest {
    
    public final String Team;
    
    public final boolean HasShowChangeLog;
    
    public JiraRequest(String team, boolean hasShowChangeLog)
    {
        this.Team = team;
        this.HasShowChangeLog = hasShowChangeLog;        
    }    
}
