/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.POCO.Atlassian.Rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author sdzyuban
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSprintResponse {
   
    public JiraSprint[] values;
}
