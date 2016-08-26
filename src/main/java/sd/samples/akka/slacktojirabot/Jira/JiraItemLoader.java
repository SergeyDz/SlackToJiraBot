/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import java.util.Arrays;
import java.util.concurrent.Callable;
import sd.samples.akka.slacktojirabot.Mapping.JiraIssueMapper;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Issue;

/**
 *
 * @author sdzyuban
 */
public class JiraItemLoader implements Callable<Issue> {

    private final JiraRestClient client;
    
    private final Issue issue;
    
    private final BotConfigurationInfo config;
    
    public JiraItemLoader(JiraRestClient client, Issue issue, BotConfigurationInfo config)
    {
        this.client = client;
        this.issue = issue;
        this.config = config;
    }
    
    @Override
    public Issue call() throws Exception {
        Issue result = new JiraIssueMapper(this.config).apply(this.client.getIssueClient()
                .getIssue(this.issue.Key, Arrays.asList(IssueRestClient.Expandos.CHANGELOG)).claim());
        
        return result;
    }
    
}
