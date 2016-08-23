/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot;

import sd.samples.akka.slacktojirabot.Slack.SkackEventListenerActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;

/**
 *
 * @author sdzyuban
 */
public class BotEngineRunner {
       
    public static void main(String[] args) throws Exception {
        
        BotConfigurationInfo config = new BotConfigurationInfo(args);
        
        ActorSystem system = ActorSystem.create("bot-system");
        
        ActorRef slackActor = system.actorOf(Props.create(SkackEventListenerActor.class, config, "sdzyuban"), "SlackEventListener");
        slackActor.tell("start", null);
        
          Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                system.shutdown();
                System.out.println("Shutting down ... ");
            }
        });
    }
}
