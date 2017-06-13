/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.artifactory;

import akka.actor.ActorRef;
import java.io.IOException;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Slack.SlackConnectionInfo;
import sd.samples.akka.slacktojirabot.Slack.SkackEventListenerActor;

/**
 *
 * @author sergey.d
 */
public class ArtifactoryEventListenerActor extends SkackEventListenerActor
{
    public ArtifactoryEventListenerActor(BotConfigurationInfo config, String channel) throws IOException {
        super(config, channel);
    }
    
    @Override
    protected void registeringAListener(SlackConnectionInfo connection, ActorRef senderActor) {
        
    }
        
}
