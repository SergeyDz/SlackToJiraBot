/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Slack;

import akka.actor.UntypedActor;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendAttachment;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.samples.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;

/**
 *
 * @author sdzyuban
 */
public class SlackMessageSenderActor extends UntypedActor {

    private final SlackConnectionInfo connection;
    private final BotConfigurationInfo config;
    private final  SlackChatConfiguration slackConfig;
    
    public SlackMessageSenderActor(SlackConnectionInfo connection, BotConfigurationInfo config)
    {
        this.connection = connection;
        this.config = config;
        this.slackConfig = SlackChatConfiguration.getConfiguration();
        slackConfig.withName("Jirabot");
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof SendMessage){
            SendMessage sendMessage = (SendMessage)message;
            
            connection.Session.sendMessageToUser(sendMessage.Sender, sendMessage.Message, new SlackAttachment());
            
        } else if(message instanceof SendAttachment){
            
            SendAttachment source = (SendAttachment)message;
            SlackAttachment header = new SlackAttachment();
            header.color = "#267F00";
            header.text = source.Message;
            header.markdown_in = Arrays.asList("text", "pretext");
            
            connection.Session.sendMessageToUser(source.Sender, "Sprint statistics.", header);
            
            if(source.Attachments != null && source.Attachments.size() > 0)
            {
                StringBuilder builder = new StringBuilder();
                
                source.Attachments.forEach(attachment -> {
                    if(attachment.ChangelogItems != null && !attachment.ChangelogItems.isEmpty())
                    {
                        
                        SendUndefinedMessage(builder, source.Sender);
                        
                        SlackAttachment item = new SlackAttachment();
                        //item.title = "Changelog";
                        item.text = attachment.ChangelogItems;
                        
                        connection.Session.sendMessageToUser(source.Sender, attachment.Message, item);
                    }
                    else
                    {
                        builder.append(attachment.Message);
                    }
                });
                
                SendUndefinedMessage(builder, source.Sender);
            }
            
            connection.Session.sendMessageToUser(source.Sender, ":robot_face: work done !", null);
        }
    }
       
    private void SendUndefinedMessage(StringBuilder builder, SlackUser sender)
    {
        String text = builder.toString();
        builder.setLength(0);
        
        if(!text.isEmpty()){
            connection.Session.sendMessageToUser(sender, text, new SlackAttachment());
        }
    }
    
}
