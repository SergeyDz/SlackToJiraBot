/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import scala.concurrent.Future;

import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.Slack.SlackChannelListener;

/**
 *
 * @author sdzyuban
 */
public class BotEngineRunner {
       
    public static void main(String[] args) throws Exception {
        
        BotConfigurationInfo config = new BotConfigurationInfo(args);
        
        ActorSystem system = ActorSystem.create("bot-system");
        
        List<Future<ActorRef>> actors = new ArrayList<Future<ActorRef>>();
        
        config.Channels.stream().forEach((channel) -> {
            actors.add(Futures.future(new SlackChannelListener(system, config, channel), system.dispatcher()));
        });
        
        Future<Iterable<ActorRef>> result = Futures.sequence(actors, system.dispatcher());
        
        result.onSuccess(new OnSuccess<Iterable<ActorRef>>() {

            @Override
            public void onSuccess(Iterable<ActorRef> success) throws Throwable {
                Iterator<ActorRef> i = success.iterator();
                while (i.hasNext()) {
                    ActorRef actor = i.next();
                    System.out.println("Starting SkackEventListenerActor: " + actor.path().name());
                    actor.tell("start", null);
                }
            }
        }, system.dispatcher());

          Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                system.shutdown();
                System.out.println("Shutting down ... ");
            }
        });
    }
}
