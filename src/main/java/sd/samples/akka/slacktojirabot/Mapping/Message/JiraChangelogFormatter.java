/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.joda.time.DateTime;
import sd.samples.akka.slacktojirabot.Mapping.JiraFormatter;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraChangelogItem;
import sd.samples.akka.slacktojirabot.POCO.Github.Commit;

/**
 *
 * @author sdzyuban
 */
public class JiraChangelogFormatter{

    private final List<JiraChangelogItem> logs;
    private final List<Commit> commits;
    private final BotConfigurationInfo config;
    
    private Map<DateTime, String> results;
    
    public  JiraChangelogFormatter(Issue issue, BotConfigurationInfo config)
    {
        this.logs = issue.Changelog;
        this.commits = issue.Commits;
        this.config = config;
    }
    
    public String call() {
        
        results = new HashMap<>();
        

        if(logs != null && logs.size() > 0)
        {
            //builder.append(":newspaper: ");
            logs.forEach(log -> {
                        String assignee = JiraFormatter.GetUserPic(log.Author);
                        String row = this.convertChangelogFields(log.Field, log.From, log.To);
                        if(!assignee.isEmpty() && !row.isEmpty())
                        {
                            results.put(log.Created, String.format("%s %s %s \n", log.Created.toString("MM/dd HH:mm"), assignee, row));
                        }
                    });
        }
        
        if(commits != null && commits.size() > 0)
        {
            //builder.append(":newspaper: ");
            commits.forEach(commit -> {
                        results.put(new DateTime(commit.CreatedOn),
                                String.format("%s %s-<%s> [%s]-<%s|%s> %s \n", 
                                "master".equals(commit.Branch) ? ":github_green:" : ":github_grey:",
                                new DateTime(commit.CreatedOn).toString("MM/dd HH:mm"),
                                commit.Author,
                                commit.Branch.length() > 9 ? commit.Branch.substring(0, 9) : commit.Branch,
                                commit.Url.replace("api.", "").replace("/repos", "").replace("commits", "commit"),
                                commit.Id.isEmpty() ? "empty" : commit.Id.substring(0, 4),
                                commit.Message.length() > 60 ?  commit.Message.substring(0, 60) + " ..." : commit.Message
                        ));
            });
        }
        
        
        StringBuilder builder = new StringBuilder();
        
        new TreeMap<>(results)
        .forEach((key, value) -> {
            builder.append(value);
        });
        
        String result =  builder.toString();
        return result.length() > 2 ? result.substring(0, result.length() - 2) : result;
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
            case "Comment":
            case "comment":
                result = ":memo: : " + to;
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
