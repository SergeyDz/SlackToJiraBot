/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Slack;

import com.ullink.slack.simpleslackapi.SlackUser;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class SendAttachment extends SendMessage {
    
    public List<Attachment> Attachments;

    public SendAttachment(String message) {
        super(message);
    }
}
