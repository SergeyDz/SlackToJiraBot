/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.POCO.Atlassian.Rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

/**
 *
 * @author sdzyuban
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSprint {
    
    public int id; 
    
    public String self;
    
    public String state;
    
    public String name;
    
    public Date startDate; 
    
    public Date endDate; 
}
