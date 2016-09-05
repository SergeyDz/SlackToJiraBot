/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Issue;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraStatisticsItem;

/**
 *
 * @author sdzyuban
 */
public class JiraStatisticsFormatter implements Callable<String>{
    
    private final List<Issue> issues;
    
    public JiraStatisticsFormatter(List<Issue> issues)
    {
        this.issues = issues;
    }

    @Override
    public String call() throws Exception {
       StringBuilder builder = new StringBuilder();
       
       if(this.issues != null)
       {
            builder.append(getTotalSummary(issues));
            builder.append(getAssigneeSummary(issues));
            builder.append(getStatusSummary(issues));
            builder.append("\n");
       }
       return builder.toString();
    }
    
    private String getTotalSummary(List<Issue> issues)
    {
        StringBuilder builder = new StringBuilder();
        
        Double totalStroyPoints = issues.stream().mapToDouble(i -> i.StoryPoints).sum();
        
        builder.append(String.format("*Total items*: %s _(%ssp)_. ", issues.size(), totalStroyPoints));
        issues.stream()
                .collect(Collectors.groupingBy(w -> w.IssueType))
                .entrySet().stream()
                .map(a -> new JiraStatisticsItem(
                        a.getKey(), 
                        a.getValue().size(), 
                        a.getValue().stream().mapToDouble(m -> m.StoryPoints).sum(), 
                        (100.0 * a.getValue().size()/ issues.size()) 
                ))
                .sorted(Comparator.comparingDouble(JiraStatisticsItem::getStoryPoints).reversed())
                .collect(Collectors.toList())
                .forEach(item -> {
                     builder.append(String.format("%s: %s _(%1.0f%% - %1.1fsp)_, ", item.Key, item.Count, item.Persentage, item.StoryPoints));
                });

        String result =  builder.toString();
        return result.substring(0, result.length() - 2) + "\n";
    }
    
    private String getAssigneeSummary(List<Issue> issues)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("*Assignee*: ");
        issues.stream()
                .filter(f -> !"Closed".equals(f.Status))
                .collect(Collectors.groupingBy(w -> w.Assignee))
                .entrySet().stream()
                .map(a -> new JiraStatisticsItem(
                        a.getKey(), 
                        a.getValue().size(), 
                        a.getValue().stream().filter(m ->  !"Resolved".equals(m.Status)).mapToDouble(m -> m.StoryPoints).sum(), 
                        (100.0 * a.getValue().size()/ issues.size()) 
                ))
                .sorted(Comparator.comparingDouble(JiraStatisticsItem::getStoryPoints).reversed())
                .collect(Collectors.toList())
                .forEach(item -> {
                     builder.append(String.format("%s: %s _(%1.0f%% - %1.1fsp)_, ", item.Key, item.Count, item.Persentage, item.StoryPoints));
                });

        String result =  builder.toString();
        return result.substring(0, result.length() - 2) + "\n";
    }
    
    private String getStatusSummary(List<Issue> issues)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("*Status*: ");
        issues.stream()
                .collect(Collectors.groupingBy(w -> w.Status))
                .entrySet().stream()
                .map(a -> new JiraStatisticsItem(
                        a.getKey(), 
                        a.getValue().size(), 
                        a.getValue().stream().mapToDouble(m -> m.StoryPoints).sum(), 
                        (100.0 * a.getValue().size()/ issues.size()) 
                ))
                .sorted(Comparator.comparingDouble(JiraStatisticsItem::getStoryPoints).reversed())
                .collect(Collectors.toList())
                .forEach(item -> {
                     builder.append(String.format("%s: %s _(%1.0f%% - %1.1fsp)_, ", item.Key, item.Count, item.Persentage, item.StoryPoints));
                });

        String result =  builder.toString();
        return result.substring(0, result.length() - 2) + "\n";
    }
    
}
