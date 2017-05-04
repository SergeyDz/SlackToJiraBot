/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.http.concurrent.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.http.HttpResponse;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprint;
import sd.samples.akka.slacktojirabot.POCO.Atlassian.Rest.JiraSprintResponse;

/**
 *
 * @author sdzyuban
 */
public class JiraSprintCallback implements FutureCallback<HttpResponse>{

    private final CompletableFuture<List<JiraSprint>> future;
    
    private final ObjectMapper objectMapper;
    
    public JiraSprintCallback(CompletableFuture<List<JiraSprint>> future)
    {
        this.future = future;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void completed(HttpResponse response) {
        try
        {
            InputStream content = response.getEntity().getContent();
            JiraSprintResponse sprints = objectMapper.readValue(response.getEntity().getContent(), JiraSprintResponse.class);
            future.complete(Arrays.asList(sprints.values));
        }
        catch(IOException | IllegalStateException ex)
        {
            System.err.println(ex.getMessage());
            future.completeExceptionally(ex);
        }
    }

    @Override
    public void failed(Exception ex) {
        System.err.println(ex.getMessage());
        future.completeExceptionally(ex);
    }

    @Override
    public void cancelled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
