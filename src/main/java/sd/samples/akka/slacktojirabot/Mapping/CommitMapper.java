/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import akka.dispatch.Mapper;
import org.eclipse.egit.github.core.RepositoryCommit;
import sd.samples.akka.slacktojirabot.POCO.Github.Commit;

/**
 *
 * @author sdzyuban
 */
public class CommitMapper extends Mapper<RepositoryCommit, Commit> {
        
    private final String branch;
    
    public CommitMapper(String b)
    {
        this.branch = b;
    }
    
    @Override
    public Commit apply(RepositoryCommit source)
    {
        Commit result = new Commit();
        
        if(source != null)
        {
          result.Id = source.getSha();
          result.Author = source.getCommit().getAuthor().getName();
          result.CreatedOn = source.getCommit().getCommitter().getDate();
          result.Url = source.getUrl();
          result.Message = source.getCommit().getMessage();
          result.Branch = this.branch;
        }
        
        return result;
    }
}
