/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import akka.dispatch.Mapper;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Issue;

/**
 *
 * @author sdzyuban
 */
public class JiraIssueMapper extends Mapper<com.atlassian.jira.rest.client.api.domain.Issue, Issue> {
     
    private final BotConfigurationInfo config;
    
    public JiraIssueMapper(BotConfigurationInfo config)
    {
        this.config = config;
    }
    
    @Override
    public Issue apply(com.atlassian.jira.rest.client.api.domain.Issue source) {
        Issue result =  new Issue();
        
        if(source != null)
        {
            result.Key = source.getKey();
            result.Assignee = source.getAssignee() != null ? source.getAssignee().getName() : "Unassigned";
            result.Status = source.getStatus().getName();
            result.IssueType = source.getIssueType().getName();
            result.Summary = source.getSummary();
            result.StoryPoints = getStoryPoints(source);
            
            result.Url = config.JiraBaseUrl + "/browse/" + result.Key;
        }
        
        return result; 
    }
    
    private Double getStoryPoints(com.atlassian.jira.rest.client.api.domain.Issue issue)
    {
        IssueField field = issue.getFieldByName("Story Points");
        if(field != null)
        {
            Object value = field.getValue();
            if(value != null)
            {
                return Double.parseDouble(value.toString());
            }
        }
        
        return 0.0;
    }
}
