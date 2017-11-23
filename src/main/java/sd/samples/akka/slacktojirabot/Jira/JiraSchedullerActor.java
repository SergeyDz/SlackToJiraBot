/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import org.joda.time.DateTime;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraRequest;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Slack.SlackUserRequest;

/**
 *
 * @author sergey.d
 */
public class JiraSchedullerActor extends UntypedActor {

    private final ActorRef senderActor;
    private final BotConfigurationInfo config;
    private final SlackUserRequest slackRequest;
    
    public JiraSchedullerActor(SlackUserRequest request, ActorRef senderActor, BotConfigurationInfo config)
    {
        this.senderActor = senderActor;
        this.config = config;
        this.slackRequest = request;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof JiraRequest)
        {
            JiraRequest request = (JiraRequest)message;
            DateTime showItemsModifiedOn =  new DateTime().minusMinutes(this.config.WatchSprintChangesTimeout + 1);
            ActorRef jiraActor = context().actorOf(Props.create(JiraActor.class, request, this.senderActor, showItemsModifiedOn, this.config));
            jiraActor.tell(request, null);
        }
    }
}
