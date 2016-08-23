/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.GitHub;

import akka.actor.UntypedActor;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.PullRequestService;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Issue;
import sd.samples.akka.slacktojirabot.POCO.LinkPullRequests;

/**
 *
 * @author sdzyuban
 */
public class GitHubPullRequestActor extends UntypedActor {    
    
    private final BotConfigurationInfo config;

    public GitHubPullRequestActor(BotConfigurationInfo config)
    {
        this.config = config;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof LinkPullRequests)
        {
            List<Issue> issues = ((LinkPullRequests)message).getIssues();
            
            RepositoryId repo = new RepositoryId("intappx", "intappcloud");
            PullRequestService service = new PullRequestService();
            service.getClient().setOAuth2Token(config.GitHubToken);

            List<PullRequest> pullRequests = service.getPullRequests(repo, "Open");

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
