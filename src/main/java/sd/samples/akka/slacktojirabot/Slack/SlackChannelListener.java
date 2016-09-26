/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Slack;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import java.util.concurrent.Callable;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;

/**
 *
 * @author sdzyuban
 */
public class SlackChannelListener implements Callable<ActorRef> {
    
    private final ActorSystem system;
    private final String channel;
    private final BotConfigurationInfo config;
    
    public SlackChannelListener(ActorSystem system, BotConfigurationInfo config, String channel)
    {
        this.channel = channel;
        this.config = config;
        this.system = system;
    }
    
    @Override
    public ActorRef call() throws Exception {
        System.out.println("Creting Actor for channel " + this.channel);
        return system.actorOf(new RoundRobinPool(4).props(Props.create(SkackEventListenerActor.class, config, this.channel)));
    }
    
}
