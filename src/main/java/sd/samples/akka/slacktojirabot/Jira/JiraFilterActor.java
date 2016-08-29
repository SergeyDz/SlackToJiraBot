/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.StringUtils;
import sd.samples.akka.slacktojirabot.Mapping.Attachment.JiraIssuesToAttachmentFormatter;
import sd.samples.akka.slacktojirabot.Mapping.JiraIssueMapper;
import sd.samples.akka.slacktojirabot.Mapping.Message.JiraIssuesResultFormatter;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.SendMessage;

import sd.samples.akka.slacktojirabot.POCO.*;
import sd.samples.akka.slacktojirabot.Slack.SlackChannelListener;

/**
 *
 * @author sdzyuban
 */
public class JiraFilterActor extends UntypedActor {

    private final ActorRef senderActor;
    private final ActorRef gitActor;
    private final BotConfigurationInfo config;
    
    public JiraFilterActor(ActorRef senderActor, ActorRef gitActor, BotConfigurationInfo config)
    {
        this.senderActor = senderActor;
        this.gitActor = gitActor;
        this.config = config;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof JiraFilterRequest)
        {
            JiraFilterRequest request = (JiraFilterRequest)message;
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            final URI jiraServerUri = new URI(config.JiraBaseUrl);
            final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, config.JiraUser, config.JiraPassword);
            ListenableFuture<SearchResult> searchResults = restClient.getSearchClient()
                            .searchJql(String.format("(project = \"Intapp Cloud\"  OR project=\"DevOps\") AND Sprint in openSprints() AND labels in (%s) ORDER BY status ASC", request.Sprint));

            Futures.addCallback(searchResults, new FutureCallback<SearchResult>() {
                        @Override
                        public void onSuccess(SearchResult results) {    
                            List<Issue> resWithChnagelog = new ArrayList<>();
                            List<Issue> res = StreamSupport.stream(results.getIssues().spliterator(), false)
                                    .filter(p -> p.getFieldByName("Sprint") != null 
                                            && p.getFieldByName("Sprint").getValue() != null
                                            && StringUtils.containsIgnoreCase(p.getFieldByName("Sprint").getValue().toString(), request.Sprint)  
                                    )
                                    .map(a -> new JiraIssueMapper((config)).apply(a))
                                    .collect(Collectors.toList());
                            
                            List<scala.concurrent.Future<Issue>> collect = res.stream()
                                    .map(a -> akka.dispatch.Futures.future(new JiraItemLoader(restClient, a, config), context().system().dispatcher()))
                                    .collect(Collectors.toList());
 
                            scala.concurrent.Future<Iterable<Issue>> result = akka.dispatch.Futures.sequence(collect, context().dispatcher());
        
                            result.onSuccess(new OnSuccess<Iterable<Issue>>() {

                                @Override
                                public void onSuccess(Iterable<Issue> success) throws Throwable {
                                    List<Issue> issues = StreamSupport.stream(success.spliterator(), false)
                                            .collect(Collectors.toList());
                                    
                                    gitActor.tell(new LinkPullRequests(issues), self());
                                }
                            }, context().dispatcher());

                            //gitActor.tell(new LinkPullRequests(resWithChnagelog), self());  
                        }

                        @Override
                        public void onFailure(Throwable thrwbl) {
                            System.err.println(thrwbl);
                        }

                    }); 
        }
        else if(message instanceof LinkPullRequests)
        {
            List<Issue> issues = ((LinkPullRequests)message).getIssues();
            if(config.HasUseSlackAttachment)
            {
                senderActor.tell(new JiraIssuesToAttachmentFormatter(issues, config).call(), null);
            }
            else
            {
                senderActor.tell(new SendMessage(new JiraIssuesResultFormatter(issues, config).call()), null);
            }
        }
        else
        {
             System.out.println("GitHubPullRequestActor not support message " + message);
        }
    }
}