/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Slack;

import akka.actor.UntypedActor;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.SendAttachment;
import sd.samples.akka.slacktojirabot.POCO.SendMessage;
import sd.samples.akka.slacktojirabot.POCO.SlackConnectionInfo;

/**
 *
 * @author sdzyuban
 */
public class SlackMessageSenderActor extends UntypedActor {

    private final SlackConnectionInfo connection;
    private final BotConfigurationInfo config;
    
    public SlackMessageSenderActor(SlackConnectionInfo connection, BotConfigurationInfo config)
    {
        this.connection = connection;
        this.config = config;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof SendMessage){
            SendMessage sendMessage = (SendMessage)message;
            
            if(sendMessage.Message.startsWith("bot"))
            {
                this.sendUsingPreparedMessage(sendMessage.Message);
            }
            
            else
            {
                connection.Session.sendMessage(connection.Channel, sendMessage.Message);
            }
        } else if(message instanceof SendAttachment){
            SendAttachment source = (SendAttachment)message;
            
            connection.Session.sendMessage(connection.Channel, source.Header);
            
            if(source.Attachments != null && source.Attachments.size() > 0)
            {
                source.Attachments.forEach(attachment -> {
                    if(attachment.ChangelogItems != null && !attachment.ChangelogItems.isEmpty())
                    {
                        SlackAttachment item = new SlackAttachment();
                        //item.title = "Changelog";
                        item.text = attachment.ChangelogItems;
                        
                        connection.Session.sendMessage(connection.Channel, attachment.Message, item);
                    }
                    else
                    {
                        connection.Session.sendMessage(connection.Channel, attachment.Message);
                    }
                });
            }
        }
    }
    
    public void sendUsingPreparedMessage(String message)
    {
        connection.Session.sendMessage(connection.Channel, "Hi, sir! " + message);
    }
    
}
