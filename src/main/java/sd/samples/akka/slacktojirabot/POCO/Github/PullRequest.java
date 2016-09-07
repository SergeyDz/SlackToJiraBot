/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Github;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class PullRequest {
    
    public PullRequest()
    {
        Statuses = new ArrayList<>();
    }
    
    public boolean IsMergablle; 
    
    public boolean WasMerged;
    
    public String Url;
    
    public List<Status> Statuses;
}
