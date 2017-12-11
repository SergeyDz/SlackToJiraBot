/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.POCO.Slack;
import com.ullink.slack.simpleslackapi.SlackUser;

/**
 *
 * @author sdzyuban
 */
public class SendMessage {
    
    public SendMessage(String message)
    {
        this.Message = message;
    }
    
    public String Message;
    
    public SlackUser SlackUser;
}
