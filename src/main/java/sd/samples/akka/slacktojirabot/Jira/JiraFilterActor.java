/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import sd.samples.akka.slacktojirabot.POCO.Github.LinkPullRequests;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraFilterRequest;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.StringUtils;
import sd.samples.akka.slacktojirabot.Mapping.Attachment.JiraIssuesToAttachmentFormatter;
import sd.samples.akka.slacktojirabot.Mapping.JiraIssueMapper;
import sd.samples.akka.slacktojirabot.Mapping.Message.JiraIssuesResultFormatter;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Slack.SendMessage;

import sd.samples.akka.slacktojirabot.POCO.*;

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
        if(message instanceof JiraFilterMessage)
        {
            JiraFilterMessage request = (JiraFilterMessage)message;
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            final URI jiraServerUri = new URI(config.JiraBaseUrl);
            final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, config.JiraUser, config.JiraPassword);

            ListenableFuture<SearchResult> searchResults = restClient.getSearchClient()
                            .searchJql(String.format("(project = \"Intapp Cloud\") AND Sprint=%s  ORDER BY status ASC", request.Sprint.id));

            Futures.addCallback(searchResults, new FutureCallback<SearchResult>() {
                        @Override
                        public void onSuccess(SearchResult results) {    

                            List<Issue> res = StreamSupport.stream(results.getIssues().spliterator(), false)
                                    .map(a -> new JiraIssueMapper((config)).apply(a))
                                    .collect(Collectors.toList());
                            
                            if(request.HasShowChangeLog)
                            {
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
                            }
                            else
                            {
                                gitActor.tell(new LinkPullRequests(res), self());  
                            }
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