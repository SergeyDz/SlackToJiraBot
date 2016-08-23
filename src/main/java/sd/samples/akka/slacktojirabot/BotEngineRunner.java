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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
       
        List<Future<ActorRef>> actors = config.Channels.stream()
                .map(a -> Futures.future(new SlackChannelListener(system, config, a), system.dispatcher()))
                .collect(Collectors.toList());
        
        Future<Iterable<ActorRef>> result = Futures.sequence(actors, system.dispatcher());
        
        result.onSuccess(new OnSuccess<Iterable<ActorRef>>() {

            @Override
            public void onSuccess(Iterable<ActorRef> success) throws Throwable {
                StreamSupport.stream(success.spliterator(), false)
                        .forEach(actor -> {
                            System.out.println("Starting SkackEventListenerActor: " + actor.path().name());
                            actor.tell("start", null);
                        });
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
