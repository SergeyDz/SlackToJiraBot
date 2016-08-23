/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.StringUtils;
import sd.samples.akka.slacktojirabot.Mapping.JiraIssueMapper;
import sd.samples.akka.slacktojirabot.Mapping.JiraIssuesResultFormatter;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.SendMessage;

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
                            List<Issue> res = StreamSupport.stream(results.getIssues().spliterator(), false)
                                    .filter(p -> (p.getProject().getName() == "DevOps" && request.Sprint == "devops") 
                                            ||  
                                            (p.getFieldByName("Sprint") != null 
                                            && p.getFieldByName("Sprint").getValue() != null
                                            && StringUtils.containsIgnoreCase(p.getFieldByName("Sprint").getValue().toString(), request.Sprint)
                                            )
                                    )
                                    .map(a -> new JiraIssueMapper((config)).apply(a))
                                    .collect(Collectors.toList());
                            gitActor.tell(new LinkPullRequests(res), self());  
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
            senderActor.tell(new SendMessage(new JiraIssuesResultFormatter(issues, config).call()), null);
        }
        else
        {
             System.out.println("GitHubPullRequestActor not support message " + message);
        }
    }
}