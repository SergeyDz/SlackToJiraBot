package sd.samples.akka.slacktojirabot.Slack;

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
        
        String removeReqularWords = this.message
                .replace("sprint", "")
                .replace("status", "")
                .replace("jirabot", "")
                .trim();
        
        if(removeReqularWords.isEmpty())
        {
            if(channel.startsWith("team-"))
            {
                return channel.replace("team-", "");
            }
            else if(channel.endsWith("-private"))
            {
                return channel.replace("-private", "");
            }
             else if(channel.endsWith("-sdzyuban"))
            {
                return channel.replace("-sdzyuban", "");
            }
        }
        else if(message.split(" ").length > 1)
        {
            return removeReqularWords;
        }
        
        return "";
    }
    
    
    
}
