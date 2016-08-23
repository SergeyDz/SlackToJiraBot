package sd.samples.akka.slacktojirabot.Slack;


import java.util.concurrent.Callable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sdzyuban
 */
public class WhereAmILocator {
   
    private final String message; 
    private final String channel; 
   
    public WhereAmILocator(String message, String channel)
    {
        this.message = message;
        this.channel = channel;
    }

    public String call() {
        
        if(message.equals("jirabot sprint"))
        {
            if(channel.startsWith("team-"))
            {
                return channel.replace("team-", "");
            }
            else if(channel.endsWith("-private"))
            {
                return channel.replace("-private", "");
            }
        }
        else if(message.startsWith("jirabot sprint") && message.split(" ").length > 2)
        {
            return message.split(" ")[2];
        }
        
        return "";
    }
    
    
    
}
