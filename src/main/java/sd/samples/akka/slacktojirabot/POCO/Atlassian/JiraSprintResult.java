/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Atlassian;

import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;

/**
 *
 * @author sdzyuban
 */
public class JiraSprintResult {
    
    public JiraSprintResult(JiraSprint sprint, String teamName)
    {
        this.Sprint = sprint;
        this.TeamName = teamName;
    }
    
    public final JiraSprint Sprint; 
    
    public final String TeamName;

}
