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
public class JiraFilterRequest {
    
    public final String FilterId;
    
    public final String Sprint;
    
    public final boolean HasShowChangeLog;
    
    public JiraFilterRequest(String filterId, String sprint, boolean hasShowChangeLog)
    {
        this.FilterId = filterId;
        this.Sprint = sprint;
        this.HasShowChangeLog = hasShowChangeLog;
    }
    
    
    
}