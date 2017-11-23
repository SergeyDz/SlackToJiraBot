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
import org.apache.commons.codec.binary.Base64;

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

        this.SlackAuthorizationKey = propertiesMap.containsKey("slack-key") ? propertiesMap.get("slack-key").trim() : System.getenv("slackkey");
        this.JiraUser = propertiesMap.containsKey("jira-user") ? propertiesMap.get("jira-user").trim() : System.getenv("jirauser");
        this.JiraPassword = propertiesMap.containsKey("jira-password") ? propertiesMap.get("jira-password").trim() : System.getenv("jirapassword");
       
        this.JiraObserveInterval = propertiesMap.containsKey("jiraobserveinterval") ? propertiesMap.get("jiraobserveinterval").trim() : System.getenv("jiraobserveinterval");
        
        this.JiraUser = new String(Base64.decodeBase64(this.JiraUser));
        this.JiraPassword = new String(Base64.decodeBase64(this.JiraPassword));
        
        this.AFUser = propertiesMap.containsKey("af-user") ? propertiesMap.get("af-user").trim() : System.getenv("afuser");
        this.AFPassword = propertiesMap.containsKey("af-password") ? propertiesMap.get("af-password").trim() : System.getenv("afpassword");
        
        
        String channels = propertiesMap.containsKey("slack-channels") ? propertiesMap.get("slack-channels").trim() : System.getenv("slackchannels");
        
        if(channels == null)
        {
            throw new Exception("Channels is not defined.");
        }
        
        System.out.println("Channels config detected: " + channels);
        this.Channels = Arrays.asList(channels.split(","))
                .stream().map(x -> x.trim())
                .collect(Collectors.toList());
        
        this.WatchSprintChangesTimeout = Integer.parseInt(this.JiraObserveInterval);
        
    }
    
    public String SlackAuthorizationKey; 
    
    public String JiraUser;
    
    public String JiraPassword;
    
    public String AFUser;
    
    public String AFPassword;
    
    public String JiraObserveInterval;
    
    public String JiraBaseUrl = "https://jira.sbtech.com";
    
    public int ChangelogDays = 1;
    
    public boolean HasUseSlackAttachment = true;
    
    public String BotName = "JiraBot";

    public List<String> Channels;
    
    public List<String> Boards = Arrays.asList("519", "514");
    
    public int WatchSprintChangesTimeout = 10;
}
