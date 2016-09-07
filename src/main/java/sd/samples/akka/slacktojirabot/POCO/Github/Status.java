/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Github;

/**
 *
 * @author sdzyuban
 */
public class Status {
    
    public Status(String name, String url)
    {
        this.Name = name;
        this.Url = url;
    }
    
    public String Name; 
    
    public String Url;
}
