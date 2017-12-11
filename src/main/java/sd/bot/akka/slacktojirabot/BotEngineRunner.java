/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import sd.bot.akka.slacktojirabot.Engine.BotEngineActor;
import sd.bot.akka.slacktojirabot.Engine.BotEngineShutdown;
import sd.bot.akka.slacktojirabot.Engine.EngineConfiguration;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
/**
 *
 * @author sdzyuban
 */
public class BotEngineRunner {
       
    public static void main(String[] args) throws Exception {
        
        BotConfigurationInfo config = new BotConfigurationInfo(args);
        
        ActorSystem system = ActorSystem.create("bot-system");
        
        EngineConfiguration engineConfig = new EngineConfiguration(config);
        ActorRef botEngineActor = system.actorOf(Props.create(BotEngineActor.class), "BotEngineActor");
        botEngineActor.tell(engineConfig, null);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                botEngineActor.tell(new BotEngineShutdown("Bye"), null);
                system.shutdown();
                System.out.println("Shutting down ... ");
            }
        });
    }
}
