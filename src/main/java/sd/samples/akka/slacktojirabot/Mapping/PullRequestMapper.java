/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import akka.dispatch.Mapper;
import java.util.List;
import org.eclipse.egit.github.core.CommitStatus;
import sd.samples.akka.slacktojirabot.POCO.Github.PullRequest;
import sd.samples.akka.slacktojirabot.POCO.Github.Status;

/**
 *
 * @author sdzyuban
 */
public class PullRequestMapper extends Mapper<org.eclipse.egit.github.core.PullRequest, PullRequest>{
    
    private final List<CommitStatus> statuses; 
    
    public PullRequestMapper(List<CommitStatus> statuses)
    {
        this.statuses = statuses;
    }
    
    @Override
    public PullRequest apply(org.eclipse.egit.github.core.PullRequest source)
    {
        PullRequest result = new PullRequest();
        
        if(source != null)
        {
            result.IsMergablle = source.isMergeable();
            result.WasMerged = source.isMerged();
            result.Url = source.getHtmlUrl();
            
            if(this.statuses != null && !this.statuses.isEmpty())
            {
                this.statuses.forEach(s -> {
                    result.Statuses.add(new Status(s.getState(), s.getTargetUrl()));
                });
            }
            
        }
        
        return result;
    }
}
