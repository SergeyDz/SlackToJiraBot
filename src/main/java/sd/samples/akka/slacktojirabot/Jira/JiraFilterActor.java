/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintResult;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
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
import org.joda.time.DateTime;
import sd.samples.akka.slacktojirabot.Mapping.JiraIssueMapper;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraFilterResult;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;


/**
 *
 * @author sdzyuban
 */
public class JiraFilterActor extends UntypedActor {

    private final BotConfigurationInfo config;
    private final boolean hasShowChangeLog;
    private final DateTime ShowItemsModifiedOn;
    
    public JiraFilterActor(boolean hasShowChangeLog, DateTime showItemsModifiedOn, BotConfigurationInfo config)
    {
        this.config = config;
        this.hasShowChangeLog = hasShowChangeLog;
        this.ShowItemsModifiedOn = showItemsModifiedOn;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof JiraSprintResult)
        {
            JiraSprintResult request = (JiraSprintResult)message;
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            final URI jiraServerUri = new URI(config.JiraBaseUrl);
            final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, config.JiraUser, config.JiraPassword);

            ListenableFuture<SearchResult> searchResults = restClient.getSearchClient()
                            .searchJql(String.format("Sprint=%s  ORDER BY status ASC", request.Sprint.id));
            
            ActorRef sender = sender();

            Futures.addCallback(searchResults, new FutureCallback<SearchResult>() {
                        @Override
                        public void onSuccess(SearchResult results) {    
                            
                            List<Issue> res = StreamSupport.stream(results.getIssues().spliterator(), false)
                                    .map(a -> new JiraIssueMapper((config)).apply(a))
                                    .collect(Collectors.toList());
                            
                            if(hasShowChangeLog)
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
                                        
                                        if(ShowItemsModifiedOn != null)
                                        {
                                            issues
                                                    .forEach(a -> a.Changelog = a.Changelog
                                                                                        .stream()
                                                                                        .filter(b -> b.Created.isAfter(ShowItemsModifiedOn))
                                                                                        .collect(Collectors.toList()));

                                            issues = issues
                                                    .stream()
                                                    .filter(a -> !a.Changelog.isEmpty())
                                                    .collect(Collectors.toList());
                                        }
                                        
                                        sender.tell(new JiraFilterResult(issues), null);
                                        //gitActor.tell(new LinkPullRequests(new JiraIssuesContainer(issues), ((JiraSprintsResult) message).HasShowChangeLog), self());
                                        
                                    }
                                }, context().dispatcher());
                            }
                            else
                            {
                                sender.tell(new JiraFilterResult(res), null);  
                            }
                        }

                        @Override
                        public void onFailure(Throwable thrwbl) {
                            System.err.println(thrwbl);
                        }

                    }); 
        }
//        else if(message instanceof LinkPullRequests)
//        {
//            LinkPullRequests request = ((LinkPullRequests)message);
//            List<Issue> issues = request.getIssues();
//            if(config.HasUseSlackAttachment)
//            {
//                senderActor.tell(new JiraIssuesToAttachmentFormatter(new JiraIssuesContainer(issues) , config).call(), null);
//            }
//            else
//            {
//                senderActor.tell(new SendMessage(new JiraIssuesResultFormatter(issues, config).call()), null);
//            }
//        }
        else
        {
             System.out.println("GitHubPullRequestActor not support message " + message);
        }
    }
}