/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Atlassian;

import com.ullink.slack.simpleslackapi.SlackUser;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;

/**
 *
 * @author sdzyuban
 */
public class JiraSprintsResult {
    
    public JiraSprintsResult(JiraSprint sprint)
    {
        this.Sprint = sprint;
    }
    
    public final JiraSprint Sprint; 

}
