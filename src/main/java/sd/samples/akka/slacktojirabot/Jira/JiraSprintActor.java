/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintRequest;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.JiraSprintResult;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
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
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprintResponse;
import sd.samples.akka.slacktojirabot.Slack.NotFoundMessage;

/**
 *
 * @author sdzyuban
 */
public class JiraSprintActor extends UntypedActor {

    private final BotConfigurationInfo config;
    
    private final ObjectMapper objectMapper;
    
    public JiraSprintActor(BotConfigurationInfo config)
    {
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }
     
    @Override
    public void onReceive(Object message) throws Exception {
        
        ActorRef sender = sender();
        
        if(message instanceof JiraSprintRequest)
        {
            JiraSprintRequest sprint = (JiraSprintRequest)message;
            
            System.out.println("JiraSprintActor started.");
            CacheConfig cacheConfig = new CacheConfig();
            HttpAsyncClient client  = new CachingHttpAsyncClient(cacheConfig);

            String token = new String(Base64.encodeBase64((config.JiraUser + ":" + config.JiraPassword).getBytes()));

            HttpUriRequest request = new HttpGet(config.JiraBaseUrl + "/rest/agile/latest/board/2/sprint?state=active");
            request.addHeader(new BasicHeader("Authorization", "Basic " + token));
            HttpContext context = new BasicHttpContext();

            FutureCallback<HttpResponse> callback = new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse response) {
                    try
                    {
                        JiraSprintResponse sprints = objectMapper.readValue(response.getEntity().getContent(), JiraSprintResponse.class);
                        if(sprints != null && sprints.values != null)
                        {
                            Optional<JiraSprint> result = Arrays.stream(sprints.values)
                                    .filter(c -> StringUtils.containsIgnoreCase(c.name, sprint.TeamName))
                                    .findFirst();
                            
                            if(result.isPresent())
                            {
                                sender.tell(new JiraSprintResult(result.get(), sprint.TeamName), null);
                            }
                            else
                            {
                                sender.tell(new NotFoundMessage("Sprint not found. Team Name: " + sprint.TeamName), null);
                            }
                        }
                    }
                    catch(IOException | IllegalStateException ex)
                    {
                        System.err.println(ex.getMessage());
                    }
                }

                @Override
                public void failed(Exception ex) {
                    System.err.println(ex.getMessage());
                }

                @Override
                public void cancelled() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };

            client.start();
            client.execute(request, context, callback);
            }
    } 
}
