/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.GitHub;

import akka.actor.UntypedActor;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.eclipse.egit.github.core.CommitStatus;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.joda.time.DateTime;
import org.joda.time.Days;
import sd.samples.akka.slacktojirabot.Mapping.CommitMapper;
import sd.samples.akka.slacktojirabot.Mapping.PullRequestMapper;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.Github.Commit;
import sd.samples.akka.slacktojirabot.POCO.Github.LinkPullRequests;

/**
 *
 * @author sdzyuban
 */
public class GitHubPullRequestActor extends UntypedActor {    
    
    private final BotConfigurationInfo config;
    
    private final LoadingCache<String, List<PullRequest>> cachedPullRequests;
    private final LoadingCache<String, List<RepositoryCommit>> cachedCommits;
    private final LoadingCache<String, List<RepositoryBranch>> cachedBranches;
    private final LoadingCache<Integer, PullRequest> cachedPullRequest;
    
    RepositoryId repo = new RepositoryId("intappx", "intappcloud");
    RepositoryService repositoryService = new RepositoryService();
    PullRequestService pullRequestService = new PullRequestService();
    CommitService commitService  = new CommitService();
    
    public GitHubPullRequestActor(BotConfigurationInfo config)
    {
        this.config = config;

        pullRequestService.getClient().setOAuth2Token(config.GitHubToken);        
        commitService.getClient().setOAuth2Token(config.GitHubToken);
        repositoryService.getClient().setOAuth2Token(config.GitHubToken);
        
        cachedBranches = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .weakKeys()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(
            new CacheLoader<String, List<RepositoryBranch>>() {
              @Override
              public List<RepositoryBranch> load(String status) throws Exception {
                  List<RepositoryBranch> branches = repositoryService.getBranches(repo);
                  System.err.println("CacheLoader<Branches>(). Found: " + branches.size());
                  return branches;
              }
            });
        
        cachedPullRequests = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .weakKeys()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(
            new CacheLoader<String, List<PullRequest>>() {
              @Override
              public List<PullRequest> load(String status) throws Exception {
                return pullRequestService.getPullRequests(repo, status);
              }
        });
        
        cachedPullRequest = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .weakKeys()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(
            new CacheLoader<Integer, PullRequest>() {
              @Override
              public PullRequest load(Integer id) throws Exception {
                return pullRequestService.getPullRequest(repo, id);
              }
        });
        
        cachedCommits = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .weakKeys()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(
            new CacheLoader<String, List<RepositoryCommit>>() {
              @Override
              public List<RepositoryCommit> load(String key) throws Exception {
                System.out.print("CacheLoader<Commits>(" + key + "). Found: ");
                    PageIterator<RepositoryCommit> page = commitService.pageCommits(repo, key, "", 10);
                    if(page.hasNext())
                    {
                        return new ArrayList<>(page.next());
                    }
                    return null;
              }
            });
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof LinkPullRequests)
        {
            LinkPullRequests request = (LinkPullRequests)message;
            
            List<Issue> issues = request.getIssues();
            
            if(request.HasShowChangeLog)
            {
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
                        try {
                            PullRequest pull = cachedPullRequest.get(matches.get().getNumber());
                            
                            List<CommitStatus> statuses = commitService.getStatuses(repo, pull.getHead().getSha());
                            issue.PullRequests.add(new PullRequestMapper(statuses).apply(pull));
                            
                        } catch (ExecutionException ex) {
                            Logger.getLogger(GitHubPullRequestActor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(GitHubPullRequestActor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
            
            if(request.HasShowChangeLog)
            {
                List<RepositoryBranch> branches = cachedBranches.get("Active");
                
                issues.stream().forEach((issue) -> {
                    branches
                            .stream()
                            .filter(p -> 
                                    StringUtils.containsIgnoreCase(p.getName(), issue.Key)
                                    || StringUtils.containsIgnoreCase(p.getName(), issue.Key.replace("-", " "))
                                    || StringUtils.containsIgnoreCase(p.getName(), issue.Key.replace("-", ""))
                                    || p.getName().contains("master")
                            )
                            .collect(Collectors.toList())
                            .forEach(a -> {
                                try {
                                    System.out.println("Branch detected: " + a.getName());
                                    List<Commit> commits = cachedCommits.get(a.getName())
                                            .stream()
                                            .map(m -> {return new CommitMapper(a.getName()).apply(m);})
                                            .filter(p -> 
                                                    (StringUtils.containsIgnoreCase(p.Message, issue.Key)
                                                    || StringUtils.containsIgnoreCase(p.Message, issue.Key.replace("-", " "))
                                                    || StringUtils.containsIgnoreCase(p.Message, issue.Key.replace("-", "")))
                                                    && Days.daysBetween(new DateTime(p.CreatedOn), new DateTime()).isLessThan(Days.days(config.ChangelogDays))
                                            )
                                            .collect(Collectors.toList());
                                    issue.Commits.addAll(commits);
                                } 
                                catch (ExecutionException ex) {
                                    System.err.println(ex);
                                    Logger.getLogger(GitHubPullRequestActor.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            });
                    
                });
            }
            
            // call Jira Actor back
            getSender().tell(new LinkPullRequests(issues, request.HasShowChangeLog), null);
            
        }
        else
        {
            System.out.println("JiraFilterActor not support message " + message);
        }
    }
    
    
    
}
