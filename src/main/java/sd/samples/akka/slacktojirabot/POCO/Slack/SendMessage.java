/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Slack;
import java.util.List;

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

}
