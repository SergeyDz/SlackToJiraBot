/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import akka.dispatch.Mapper;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraChangelogItem;

/**
 *
 * @author sdzyuban
 */
public class JiraChangelogMapper extends Mapper<com.atlassian.jira.rest.client.api.domain.ChangelogGroup, List<JiraChangelogItem>>{
    
    private final BotConfigurationInfo config;
    
    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    
    public JiraChangelogMapper(BotConfigurationInfo config)
    {
        this.config = config;
    }
    
    @Override
    public List<JiraChangelogItem> apply(com.atlassian.jira.rest.client.api.domain.ChangelogGroup source) {
        List<JiraChangelogItem> results = new ArrayList<>();
        
        if(source != null)
        {
            String author = source.getAuthor().getName();
            DateTime created = source.getCreated();
            
            
            boolean isTimeelapsedOk = Days.daysBetween(created, new DateTime())
                        .isLessThan(Days.days(config.ChangelogDays));
            
            if(isTimeelapsedOk && source.getItems() != null)
            {
                source.getItems().forEach(item -> results.add(
                        new JiraChangelogItem(created, author, item.getFieldType().toString(), item.getField(), item.getFrom(), item.getTo())));
            }
        }
        
        return results;
    }
}
