/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Slack;

/**
 *
 * @author sdzyuban
 */
public class SlackUserRequest {
    
    public SlackUserRequest(String teamName, boolean hasShowChangeLog)
    {
        this.TeamName = teamName;
        this.HasShowChangeLog = hasShowChangeLog;
    }
    
    public final String TeamName;
    
    public final boolean HasShowChangeLog;
}
