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
import sd.bot.akka.slacktojirabot.Slack.Listeners.SlackStatusWatchActor;

/**
 *
 * @author sdzyuban
 */
public class SlackJiraStatusWatchResolver implements Callable<ActorRef> {
    
    private final ActorSystem system;
    private final String channel;
    private final BotConfigurationInfo config;
    
    public SlackJiraStatusWatchResolver(ActorSystem system, BotConfigurationInfo config, String channel)
    {
        this.channel = channel;
        this.config = config;
        this.system = system;
    }
    
    @Override
    public ActorRef call() throws Exception {
        System.out.println("DEPRECATED. Creting Actor<SlackJiraStatusWatchResolver> for channel " + this.channel);
        if(!channel.contains("artifactory"))
        {
             return system.actorOf(Props.create(SlackStatusWatchActor.class, config, this.channel), "SlackStatusWatchActor-" + this.channel);
        }
        return null;
    }
    
}
