/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Artifactory;

import akka.actor.UntypedActor;
import com.ullink.slack.simpleslackapi.SlackFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClient;
import org.jfrog.artifactory.client.model.RepoPath;
import sd.bot.akka.slacktojirabot.POCO.BotConfigurationInfo;
import sd.bot.akka.slacktojirabot.POCO.Slack.SendMessage;

/**
 *
 * @author sergey.d
 */
public class ArtifactoryUploadActor extends UntypedActor{
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
    private final String localPath = "./";
    private final String artifactoryBaseUrl = "http://artifactory.sbtech.com:8081/artifactory";
    private final String artifactoryFeatureRepository = "SBTechFeature";
    private final String artifactoryReleaseRepository = "SBTech";
    
    private String afUser = "none";
    private String afPassword = "none";
    
    protected final BotConfigurationInfo config;
    
    public ArtifactoryUploadActor(BotConfigurationInfo config)
    {
        this.config = config;
        this.afUser = config.AFUser;
        this.afPassword = config.AFPassword;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof SlackFile)
        {
            SlackFile file = (SlackFile)message;
            String downloadUrl = file.getUrlPrivate();
               
            String fileName = file.getName();
            String filePath = localPath + "/" + fileName;
            
            uploadFileToArtifactory(downloadUrl, filePath, fileName);          
        }
    }
    
    private void uploadFileToArtifactory(String downloadUrl, String filePath, String fileName) {
        try
        {
            String id = "";
            String version = "";
            String targetRepository = artifactoryFeatureRepository;
            
            downloadFromUrl(new URL(downloadUrl), filePath);
            
            Artifactory artifactory = ArtifactoryClient.create(artifactoryBaseUrl, afUser, afPassword);
            
            
            java.io.File file = new java.io.File(filePath);
                
            ZipFile zipFile = new ZipFile(filePath);

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(!entry.getName().isEmpty() && entry.getName().endsWith(".nuspec"))
                {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    String nuspec = convertStreamToString(inputStream);
                    id = getXmlValue(nuspec, "id");
                    version = getXmlValue(nuspec, "version");

                    if(!isFeaturePackage(version))
                    {
                        targetRepository = artifactoryReleaseRepository;
                    }
                }
            }

            List<RepoPath> searchItems = artifactory.searches()
            .repositories(targetRepository)
            .artifactsByName(fileName)
            .doSearch();
            
            if(searchItems.isEmpty())
            {    
                String fullUrl = String.format("%s/webapp/#/artifacts/browse/tree/General/%s/%s/%s", artifactoryBaseUrl, targetRepository, id, fileName);
                artifactory.repository(targetRepository).upload(String.format("%s/%s", id, fileName), file).doUpload();
                sender().tell(new SendMessage(String.format(":artifactory: package upload to %s completed: <%s|%s>", targetRepository, fullUrl, fileName)), null);
            }
            else
            {
                 sender().tell(new SendMessage(String.format(":artifactory: :no_entry_sign: package already exists: %s", fileName)), null);
            }
        }
        catch(Exception ex)
        {
            System.err.println(ex);
        }
    }
    
    void downloadFromUrl(URL url, String localFilename) throws IOException 
    {
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            URLConnection urlConn = url.openConnection();//connect
            urlConn.setRequestProperty ("Authorization", "Bearer " + config.SlackAuthorizationKey);

            is = urlConn.getInputStream();               //get connection inputstream
            fos = new FileOutputStream(localFilename);   //open outputstream to local file

            byte[] buffer = new byte[4096];              //declare 4KB buffer
            int len;

            //while we have availble data, continue downloading and storing to local file
            while ((len = is.read(buffer)) > 0) {  
                fos.write(buffer, 0, len);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }  
    
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    static String getXmlValue(String source, String tag)
    {
        Pattern pattern = Pattern.compile(String.format("<%s>(.+?)</%s>", tag, tag));
        Matcher matcher = pattern.matcher(source);
        matcher.find();
        return matcher.group(1); 
    }
    
    static Boolean isFeaturePackage(String version)
    {
        Pattern pattern = Pattern.compile("[-]");
        Matcher matcher = pattern.matcher(version);
        
        return version.contains("-");
    }
    
}
