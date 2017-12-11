/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.POCO.Atlassian;

import sd.bot.akka.slacktojirabot.POCO.Slack.SlackUserRequest;

/**
 *
 * @author sdzyuban
 */
public class JiraRequest extends SlackUserRequest { 

    public JiraRequest(String teamName, boolean hasShowChangeLog) {
        super(teamName, hasShowChangeLog);
    }
}
