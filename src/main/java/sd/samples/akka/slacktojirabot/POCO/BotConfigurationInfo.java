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
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author sdzyuban
 */
public class BotConfigurationInfo {
    
    private static Map<String, String> propertiesMap;
     
    public BotConfigurationInfo(String[] args)
    {
        propertiesMap = new HashMap<>();
        this.Channels = new ArrayList<String>();
        
        for (String arg : args) {
            if (arg.contains("=")) {
                propertiesMap.put(arg.substring(0, arg.indexOf('=')),
                        arg.substring(arg.indexOf('=') + 1));
            }
        }
        
        this.SlackAuthorizationKey = propertiesMap.get("slack-key").trim();
        this.JiraUser = propertiesMap.get("jira-user").trim();
        this.JiraPassword = propertiesMap.get("jira-password").trim();
        this.GitHubToken = propertiesMap.get("github-key").trim();
        
        String channels = propertiesMap.get("slack-channels").trim();
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
    
    public String GitHubToken;

    public List<String> Channels;
}
