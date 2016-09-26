/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintRequest;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import sd.samples.akka.slacktojirabot.POCO.*;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintResult;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;
import sd.samples.akka.slacktojirabot.Slack.NotFoundMessage;

/**
 *
 * @author sdzyuban
 */
public class JiraSprintActor extends UntypedActor {

    private final BotConfigurationInfo config;
    
    private final ObjectMapper objectMapper;
    
    private List<String> boards = Arrays.asList("2", "17");
    
    public JiraSprintActor(BotConfigurationInfo config)
    {
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }
     
    @Override
    public void onReceive(Object message) throws Exception {
        
        ActorRef sender = sender();
        
        List<CompletableFuture<List<JiraSprint>>> futures = new ArrayList<>();
        
        if(message instanceof JiraSprintRequest)
        {
            JiraSprintRequest sprint = (JiraSprintRequest)message;
            
            System.out.println("JiraSprintActor started.");
            CacheConfig cacheConfig = new CacheConfig();
            HttpAsyncClient client  = new CachingHttpAsyncClient(cacheConfig);
            String token = new String(Base64.encodeBase64((config.JiraUser + ":" + config.JiraPassword).getBytes()));
            BasicHeader authorization = new BasicHeader("Authorization", "Basic " + token);
            
            client.start();

            boards.forEach(a -> {
                HttpUriRequest request = new HttpGet(config.JiraBaseUrl + "/rest/agile/latest/board/" + a + "/sprint?state=active");
                request.addHeader(authorization);
                HttpContext context = new BasicHttpContext();
                
                CompletableFuture<List<JiraSprint>> future = new CompletableFuture<>();
                FutureCallback<HttpResponse> callback = new JiraSprintCallback(future);
                futures.add(future);
                client.execute(request, context, callback);
            });
            
            processCallback(futures, sprint, sender); 
        }
    }

    private void processCallback(List<CompletableFuture<List<JiraSprint>>> futures, JiraSprintRequest sprint, ActorRef sender) {
        all(futures).thenAccept(list -> {
            Optional<JiraSprint> r = list.stream()
                    .filter(c -> StringUtils.containsIgnoreCase(c.name, sprint.TeamName)
                            || (StringUtils.containsIgnoreCase(c.name, "Sprint") && (StringUtils.containsIgnoreCase(sprint.TeamName, "DevOps"))))
                    .findFirst();
            
            if(r.isPresent())
            {
                sender.tell(new JiraSprintResult(r.get(), sprint.TeamName), null);
            }
            else
            {
                sender.tell(new NotFoundMessage("Active sprint not found. Team Name: " + sprint.TeamName), null);
            }
        });
    }
    
    public static <T> CompletableFuture<List<T>> all(List<CompletableFuture<List<T>>> futures) {
    CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);

    return CompletableFuture.allOf(cfs)
            .thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList())
            );
    }
}
