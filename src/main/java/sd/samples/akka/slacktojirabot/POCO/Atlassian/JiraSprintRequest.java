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
public class JiraSprintRequest {
    
    public JiraSprintRequest(String teamName)
    {
        this.TeamName = teamName;
    }
    
    public final String TeamName;
}
