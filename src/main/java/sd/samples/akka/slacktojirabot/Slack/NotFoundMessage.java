/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Slack;

import com.ullink.slack.simpleslackapi.SlackUser;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;

/**
 *
 * @author sdzyuban
 */
public class NotFoundMessage extends SendMessage{
    
    public NotFoundMessage(String message) {
        super(message);
    }
    
}
