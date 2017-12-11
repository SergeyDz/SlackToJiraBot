/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Slack.Listeners.Resolvers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.util.concurrent.Callable;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.Artifactory.ArtifactoryEventListenerActor;
import sd.bot.akka.slacktojirabot.Slack.Listeners.SlackEventListenerActor;

/**
 *
 * @author sdzyuban
 */
public class SlackChannelListenerResolver implements Callable<ActorRef> {
    
    private final ActorSystem system;
    private final String channel;
    private final BotConfigurationInfo config;
    
    public SlackChannelListenerResolver(ActorSystem system, BotConfigurationInfo config, String channel)
    {
        this.channel = channel;
        this.config = config;
        this.system = system;
    }
    
    @Override
    public ActorRef call() throws Exception {
        System.out.println("Creting Actor<SlackChannelListenerResolver> for channel " + this.channel);
        if(channel.contains("artifactory"))
        {
            return system.actorOf(Props.create(ArtifactoryEventListenerActor.class, config, this.channel));
        }
        else
        {
            return system.actorOf(Props.create(SlackEventListenerActor.class, config, this.channel));
        }
    }
    
}
