/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Mapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import sd.samples.akka.slacktojirabot.POCO.Issue;

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
        
        issues.stream().collect(Collectors.groupingBy(w -> w.IssueType))
                .forEach((key, value) -> {
                    builder.append(String.format("%s: %s _(%1.0f%%)_, ", key, value.size(), (100.0 * value.size()/ issues.size() )));
                });
        
        String result =  builder.toString();
        return result.substring(0, result.length() - 2) + "\n";
    }
    
    private String getAssigneeSummary(List<Issue> issues)
    {
        StringBuilder builder = new StringBuilder();
        
        Double totalStroyPoints = issues.stream().mapToDouble(i -> i.StoryPoints).sum();
        
        
        Map<String, List<Issue>> group = issues.stream()
                .filter(f -> !"Closed".equals(f.Status))
                .collect(Collectors.groupingBy(w -> w.Assignee));
        builder.append(String.format("*Assignee*: %s. ", group.size()));
        
        group.forEach((key, value) -> {
                    builder.append(String.format("%s: %s _(%1.0f%% - %1.1fsp)_, ", 
                            key, value.size(), 
                            (100.0 * value.size()/ issues.size()), 
                            value.stream().filter(m ->  !"Resolved".equals(m.Status)).mapToDouble(m -> m.StoryPoints).sum() ));
                });
        
        String result =  builder.toString();
        return result.substring(0, result.length() - 2) + "\n";
    }
    
    private String getStatusSummary(List<Issue> issues)
    {
        StringBuilder builder = new StringBuilder();
        
        Double totalStroyPoints = issues.stream().mapToDouble(i -> i.StoryPoints).sum();
        
        builder.append(String.format("*Status*: %s. ", issues.stream().collect(Collectors.groupingBy(w -> w.Status)).size()));
        
        issues.stream().collect(Collectors.groupingBy(w -> w.Status))
                .forEach((key, value) -> {
                    builder.append(String.format("%s: %s _(%1.0f%%)_, ", key, value.size(), (100.0 * value.size()/ issues.size())));
                });
        
        String result =  builder.toString();
        return result.substring(0, result.length() - 2) + "\n";
    }
    
}
