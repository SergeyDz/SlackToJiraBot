/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO;

/**
 *
 * @author sdzyuban
 */
public class BotConfigurationInfo {
    
    public BotConfigurationInfo(String[] args)
    {
        this.SlackAuthorizationKey = args[0];
        this.JiraUser = args[1];
        this.JiraPassword = args[2];
        this.GitHubToken = args[3];
    }
    
    public String SlackAuthorizationKey; 
    
    public String JiraUser;
    
    public String JiraPassword;
    
    public String JiraBaseUrl = "https://intapp.atlassian.net";
    
    public String GitHubToken;
}
