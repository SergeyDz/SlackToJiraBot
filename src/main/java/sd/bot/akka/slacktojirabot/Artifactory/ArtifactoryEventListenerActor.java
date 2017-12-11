/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Artifactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import java.io.IOException;
import java.net.MalformedURLException;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.bot.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;
import sd.bot.akka.slacktojirabot.Slack.Senders.SlackChannelMessageSenderActor;
/**
 *
 * @author sergey.d
 */
public class ArtifactoryEventListenerActor extends akka.actor.UntypedActor
{
    protected final BotConfigurationInfo config;
    protected final String channel;
    private SlackSession session;
    private String localPath = "./files/";
    ActorRef channelSenderActor = null;
    
    public ArtifactoryEventListenerActor(BotConfigurationInfo config, String channel) throws IOException {
        
        this.config = config;
        this.channel = channel;
    }
    
    @Override
    public void onReceive(Object message) throws Exception , IOException {
        if(message instanceof String && "start".equals(message))
        {
            session = SlackSessionFactory.createWebSocketSlackSession(this.config.SlackAuthorizationKey);
            session.connect();

            SlackChannel theChannel = session.findChannelByName(this.channel);
            SlackConnectionInfo connection = new SlackConnectionInfo(session, theChannel);
            
            channelSenderActor = context().actorOf(Props.create(SlackChannelMessageSenderActor.class, connection, this.config), "ChannelSenderActor-" + this.channel);
            channelSenderActor.tell(new SendMessage(String.format("Connected %s!", this.channel)), null);

            registeringAListener(connection, channelSenderActor);
            System.out.println("Connection success");
        } 
        else if (message instanceof SendMessage)
        {
            channelSenderActor.tell(message, null);
        }
    }
    
    protected void registeringAListener(SlackConnectionInfo connection, ActorRef senderActor) throws IOException, MalformedURLException {
         // first define the listener
        SlackMessagePostedListener messagePostedListener = (SlackMessagePosted event, SlackSession s) -> {
            if (!connection.Channel.getId().equals(event.getChannel().getId())) {
                return; 
            }
            String messageContent = event.getMessageContent().toLowerCase();
            SlackUser sender = event.getSender();
            
            if(event.getSlackFile() != null)
            {
               ActorRef uploader = context().actorOf(Props.create(ArtifactoryUploadActor.class, config));
               uploader.tell(event.getSlackFile(), self());     
            }
            
        };
        
         session.addMessagePostedListener(messagePostedListener);
    }    
}
