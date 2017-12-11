/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Slack.Senders;

import akka.actor.UntypedActor;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.bot.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;

/**
 *
 * @author sdzyuban
 */
public class SlackChannelMessageSenderActor extends UntypedActor {

    private final SlackConnectionInfo connection;
    private final BotConfigurationInfo config;
    private final  SlackChatConfiguration slackConfig;
    private final SlackChannel channel;
    
    public SlackChannelMessageSenderActor(SlackConnectionInfo connection, BotConfigurationInfo config)
    {
        this.connection = connection;
        this.config = config;
        this.channel = connection.Channel;
        this.slackConfig = SlackChatConfiguration.getConfiguration();
        slackConfig.withName("sauron");
    }

    @Override
    public void onReceive(Object message) throws Exception {
         if(message instanceof SendMessage){
            SendMessage sendMessage = (SendMessage)message;
            connection.Session.sendMessage(this.channel, sendMessage.Message); 
        }
    }
}
