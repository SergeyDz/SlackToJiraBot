/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author sdzyuban
 */
public class BotConfigurationInfo {
    
    private static Map<String, String> propertiesMap;
     
    public BotConfigurationInfo(String[] args) throws Exception
    {     
        System.out.println("BotEngineRunner starting with parameters:");
        propertiesMap = new HashMap<>();
        this.Channels = new ArrayList<String>();
        
        if(args != null && args.length > 0)
        {
            for (String arg : args) {
                if (arg.contains("=")) {
                    String key = arg.substring(0, arg.indexOf('='));
                    String value = arg.substring(arg.indexOf('=') + 1);

                    System.out.println(arg);

                    propertiesMap.put(key, value);
                }
            }
        }
        
        this.SlackAuthorizationKey = propertiesMap.get("slack-key").isEmpty() ? propertiesMap.get("slack-key").trim() : System.getenv("slackkey");
        this.JiraUser = propertiesMap.get("jira-user").isEmpty() ? propertiesMap.get("jira-user").trim() : System.getenv("jirauser");
        this.JiraPassword = propertiesMap.get("jira-password").isEmpty() ? propertiesMap.get("jira-password").trim() : System.getenv("jirapassword");
        this.GitHubToken = propertiesMap.get("github-key").isEmpty() ? propertiesMap.get("github-key").trim() : System.getenv("githubkey");
        String channels = propertiesMap.get("slack-channels").isEmpty() ? propertiesMap.get("slack-channels").trim() : System.getenv("slackchannels");
        
        System.out.println("Channels config detected: " + channels);
        if(channels.isEmpty())
        {
            System.err.println("Channels is not defined.");
        }
        
        this.Channels = Arrays.asList(channels.split(","))
                .stream().map(x -> x.trim())
                .collect(Collectors.toList());
        
    }
    
    public String SlackAuthorizationKey; 
    
    public String JiraUser;
    
    public String JiraPassword;
    
    public String JiraBaseUrl = "https://intapp.atlassian.net";
    
    public int ChangelogDays = 1;
    
    public boolean HasUseSlackAttachment = true;
    
    public String GitHubToken;

    public List<String> Channels;
}
