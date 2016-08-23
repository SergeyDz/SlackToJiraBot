/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sdzyuban
 */
public class BotConfigurationInfo {
    
    public BotConfigurationInfo(String[] args)
    {
        propertiesMap = new HashMap<>();
        for (String arg : args) {
            if (arg.contains("=")) {
                propertiesMap.put(arg.substring(1, arg.indexOf('=')),
                        arg.substring(arg.indexOf('=') + 1));
            }
        }
        
        this.SlackAuthorizationKey = propertiesMap.get("slack-key").trim();
        this.JiraUser = propertiesMap.get("jira-user").trim();
        this.JiraPassword = propertiesMap.get("jira-password").trim();
        this.GitHubToken = propertiesMap.get("github-key").trim();
    }
    
    public String SlackAuthorizationKey; 
    
    public String JiraUser;
    
    public String JiraPassword;
    
    public String JiraBaseUrl = "https://intapp.atlassian.net";
    
    public String GitHubToken;
    
    private static Map<String, String> propertiesMap;
}
