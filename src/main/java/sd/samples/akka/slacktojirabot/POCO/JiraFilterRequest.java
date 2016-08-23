/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO;

/**
 *
 * @author sdzyuban
 */
public class JiraFilterRequest {
    
    public final String FilterId;
    
    public final String Sprint;
    
    public JiraFilterRequest(String filterId, String sprint)
    {
        this.FilterId = filterId;
        
        this.Sprint = sprint;
    }
    
}
