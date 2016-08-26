/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import java.util.List;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.JiraChangelogItem;

/**
 *
 * @author sdzyuban
 */
public class JiraChangelogFormatter{

    private final List<JiraChangelogItem> logs;
    private final BotConfigurationInfo config;
    
    public  JiraChangelogFormatter(List<JiraChangelogItem> logs, BotConfigurationInfo config)
    {
        this.logs = logs;
        this.config = config;
    }
    
    public String call() {
        StringBuilder builder = new StringBuilder();
        
        if(logs != null && logs.size() > 0)
        {
            builder.append(":newspaper: ");
            logs.forEach(log -> {
                        String assignee = JiraIssuesResultFormatter.getUserPic(log.Author);
                        String row = this.convertChangelogFields(log.Field, log.From, log.To);
                        if(!assignee.isEmpty() && !row.isEmpty())
                        {
                            builder.append(String.format("%s *:* %s *,* ", assignee, row));
                        }
                    });
        }
        String result =  builder.toString();
        
        return result.length() > 4 ? result.substring(0, result.length() - 4) : result;
    }
    
    private String convertChangelogFields(String field, String from, String to)
    {
        String result = "";
        
        switch(field)
        {
            case "status": 
                result = String.format("%s->%s", 
                        JiraIssuesResultFormatter.getStatusEmoji(JiraIssuesResultFormatter.getStatusTextById(from)),
                        JiraIssuesResultFormatter.getStatusEmoji(JiraIssuesResultFormatter.getStatusTextById(to)));
                break;
            case "assignee": 
                result = String.format("%s->%s", 
                        JiraIssuesResultFormatter.getUserPic(from),
                        JiraIssuesResultFormatter.getUserPic(to));
                break;
            case "resolution":
                break;
            case "Comment":
                result = ":memo: : " + to;
                break;
            default: 
                result = String.format(" %s %s->%s", field, from, to);
                break;
        }
        
        return result;
    }
}
