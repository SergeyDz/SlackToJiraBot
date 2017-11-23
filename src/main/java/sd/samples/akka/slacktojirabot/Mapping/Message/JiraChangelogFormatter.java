/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping.Message;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.print.Collation;
import org.joda.time.DateTime;
import sd.samples.akka.slacktojirabot.Mapping.JiraFormatter;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraChangelogItem;

/**
 *
 * @author sdzyuban
 */
public class JiraChangelogFormatter{

    private final List<JiraChangelogItem> logs;
    private final BotConfigurationInfo config;
    
    private Map<DateTime, String> results;
    
    public  JiraChangelogFormatter(Issue issue, BotConfigurationInfo config)
    {
        this.logs = issue.Changelog;
        this.config = config;
    }
    
    public String call() {
        
        results = new HashMap<>();
        

        if(logs != null && logs.size() > 0)
        {
            //builder.append(":newspaper: ");
            
            Collections.sort(this.logs, new Comparator<JiraChangelogItem>() {
                 @Override
                 public int compare(JiraChangelogItem o1, JiraChangelogItem o2) {
                     return o1.Created.compareTo(o2.Created);
                 }
            });
            
            Collections.reverse(this.logs);
            
            logs.forEach(log -> {
                        String assignee = JiraFormatter.GetUserPic(log.Author);
                        String row = this.convertChangelogFields(log.Field, log.From, log.To);
                        if(!assignee.isEmpty() && !row.isEmpty())
                        {
                            results.put(log.Created, String.format("\n:empty::empty::r: _%s %s %s_", log.Created.toString("MM/dd HH:mm"), assignee, row));
                        }
                    });
        }
       
        
        StringBuilder builder = new StringBuilder();
        
        new TreeMap<>(results)
        .forEach((key, value) -> {
            builder.append(value);
        });
        
        String result =  builder.toString();
        //return result.length() > 2 ? result.substring(0, result.length() - 2) : result;
        
        return result;
    }
    
    private String convertChangelogFields(String field, String from, String to)
    {
        String result = "";
        
        switch(field)
        {
            case "status": 
                result = String.format("%s:arrow:%s", 
                        JiraFormatter.GetStatusEmoji(JiraFormatter.GetStatusTextById(from)),
                        JiraFormatter.GetStatusEmoji(JiraFormatter.GetStatusTextById(to)));
                break;
                
            case "assignee": 
                result = String.format("%s:arrow:%s", 
                        JiraFormatter.GetUserPic(from),
                        JiraFormatter.GetUserPic(to));
                break;
                
            case "resolution":                
            case "Rank":
                break;
                
            case "Flagged":
                result = ":flag-al:";
                break;
                
            case "timeestimate":
            case "timeoriginalestimate":
                result = (from != null && !"null".equals(from)) || (to != null && !"null".equals(to)) 
                       ? String.format(" :estimate: %s:arrow:%s",  
                        from == null ? "" : (Integer.parseInt(from == null ? "0" : from) / 3600) + "h" , 
                        to == null ? "" : (Integer.parseInt(to == null ? "0" : to) / 3600) + "h"
                       ) 
                       : field;
                
                break;
                
            case "timespent":
                result = (from != null && !"null".equals(from)) || (to != null && !"null".equals(to)) 
                       ? String.format(" :log_time: %s",  
                        to == null ? "" : (Integer.parseInt(to == null ? "0" : to) / 3600) + "h"  
                       
                       ) 
                       : field;
                
                break;
            
            case "Attachment":
                 result = ":attachment:";
                 break;
                 
            case "Link":
                 result = ":link: " + ":arrow: " + to;
                 break;
                 
            case "priority":
                 result = ":priority: " + from + " :arrow: " + to;
                 break;
                 
            case "Comment":
            case "summary":
            case "Description":
            case "description":
            case "comment":
                result = ":memo:";
                break;
                
            default: 
                result = (from != null && !"null".equals(from)) || (to != null && !"null".equals(to)) 
                        ? String.format(" %s %s:arrow:%s", 
                        field, 
                        from == null ? "" : from, 
                        to == null ? "" : to)
                        : field;
                break;
        }
        
        return result;
    }
}
