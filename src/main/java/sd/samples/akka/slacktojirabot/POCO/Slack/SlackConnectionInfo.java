/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Slack;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;

/**
 *
 * @author sdzyuban
 */
public class SlackConnectionInfo {
    
    public SlackConnectionInfo(SlackSession session, SlackChannel channel)
    {
        this.Channel = channel;
        
        this.Session = session;
    }
    
    public SlackChannel Channel; 
    
    public SlackSession Session;
}
