/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.POCO.Atlassian;

import org.joda.time.DateTime;

/**
 *
 * @author sdzyuban
 */
public class JiraChangelogItem {
    
    public JiraChangelogItem(DateTime created, String autor, String fieldType, String field, String from, String to)
    {
        this.Created = created;
        this.Author = autor;
        this.FieldType = fieldType;
        this.Field  = field;
        this.From = from;
        this.To = to;
    }
    
    public final String FieldType;
    
    public final String Field;
    
    public final String From; 
    
    public final String To;
    
    public final DateTime Created;
    
    public final String Author;
}
