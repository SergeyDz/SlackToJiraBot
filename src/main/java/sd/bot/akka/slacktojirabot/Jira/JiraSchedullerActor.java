/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Jira;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import org.joda.time.DateTime;
import sd.bot.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Slack.SendMessage;
import sd.bot.akka.slacktojirabot.POCO.Slack.SlackUserRequest;

/**
 *
 * @author sergey.d
 */
public class JiraSchedullerActor extends UntypedActor {
    private final BotConfigurationInfo config;
    private final SlackUserRequest slackRequest;
    private ActorRef senderActor;
    
    private ActorRef jiraActor;
    
    public JiraSchedullerActor(SlackUserRequest request, ActorRef senderActor, BotConfigurationInfo config)
    {
        this.config = config;
        this.slackRequest = request;
        this.senderActor = senderActor;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof JiraRequest)
        {
            JiraRequest request = (JiraRequest)message;
            DateTime showItemsModifiedOn =  new DateTime().minusMinutes(this.config.WatchSprintChangesTimeout + 1);
            
            System.out.println("DEPRECATED JiraSchedullerActor starting check for: " + showItemsModifiedOn);
            this.jiraActor = context().actorOf(Props.create(JiraActor.class, request, showItemsModifiedOn, self(), this.config));
            this.jiraActor.tell(request, null);
        }
        else if(message instanceof SendMessage)
        {
            SendMessage jiraStatus = (SendMessage)message;
            this.senderActor.tell(jiraStatus, null);
            this.jiraActor.tell(PoisonPill.getInstance(), null);
        }
    }
}
