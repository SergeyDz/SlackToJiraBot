/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import akka.dispatch.Mapper;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;

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
            result.Flagged = getFlagged(source);
            
            result.Url = config.JiraBaseUrl + "/browse/" + result.Key;
            
            if(source.getChangelog() != null)
            {
                result.Changelog.addAll(StreamSupport.stream(source.getChangelog().spliterator(), false)
                        .map(a -> new JiraChangelogMapper(config).apply(a))
                        .collect(Collectors.toList())
                        .stream().flatMap(l -> l.stream())
                        .collect(Collectors.toCollection(ArrayList::new))
                );
            }
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
    
    private List<String> getFlagged(com.atlassian.jira.rest.client.api.domain.Issue issue)
    {
        IssueField field = issue.getFieldByName("Flagged");
        if(field != null)
        {
            Object value = field.getValue();
            if(value != null)
            {
                return Arrays.asList(value.toString().split(","));
            }
        }
        
        return new ArrayList<>();
    }
}
