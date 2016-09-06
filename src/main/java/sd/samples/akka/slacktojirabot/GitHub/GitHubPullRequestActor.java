/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.GitHub;

import akka.actor.UntypedActor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.PullRequestService;
import scala.annotation.serializable;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.Github.LinkPullRequests;

/**
 *
 * @author sdzyuban
 */
public class GitHubPullRequestActor extends UntypedActor {    
    
    private final BotConfigurationInfo config;
    
    private final LoadingCache<String, List<PullRequest>> cachedPullRequests;

    public GitHubPullRequestActor(BotConfigurationInfo config)
    {
        this.config = config;
        
        RepositoryId repo = new RepositoryId("intappx", "intappcloud");
        PullRequestService service = new PullRequestService();
        service.getClient().setOAuth2Token(config.GitHubToken);
        
        cachedPullRequests = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .weakKeys()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(
            new CacheLoader<String, List<PullRequest>>() {
              @Override
              public List<PullRequest> load(String status) throws Exception {
                return service.getPullRequests(repo, status);
              }
            });
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof LinkPullRequests)
        {
            List<Issue> issues = ((LinkPullRequests)message).getIssues();
            List<PullRequest> pullRequests = cachedPullRequests.get("Open");

            issues.stream().forEach((issue) -> {
                Optional<PullRequest> matches = pullRequests
                        .stream()
                        .filter(p -> 
                                StringUtils.containsIgnoreCase(p.getTitle(), issue.Key)
                                || StringUtils.containsIgnoreCase(p.getTitle(), issue.Key.replace("-", " "))
                                || StringUtils.containsIgnoreCase(p.getTitle(), issue.Key.replace("-", ""))
                        )
                        .findFirst();
                
                if (matches.isPresent()) {
                    issue.IsPullRequest = true;
                    issue.PullRequestUrl = matches.get().getHtmlUrl();
                }
                
            });
            
            // call Jira Actor back
            getSender().tell(new LinkPullRequests(issues), null);
            
        }
        else
        {
            System.out.println("JiraFilterActor not support message " + message);
        }
    }
    
    
    
}
