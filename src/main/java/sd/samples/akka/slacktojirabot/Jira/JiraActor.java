/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;

/**
 *
 * @author sdzyuban
 */
public class JiraActor extends UntypedActor {

    private final ActorRef senderActor;
    private final BotConfigurationInfo config;
    
    public JiraActor(ActorRef senderActor, BotConfigurationInfo config)
    {
        this.senderActor = senderActor;
        this.config = config;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        
        if(message instanceof JiraRequest)
        {
            senderActor.tell(new SendMessage("Hello there. I'm going to get jira results"), null);
        }
    }
    
}
