/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.POCO.Atlassian;

/**
 *
 * @author sdzyuban
 */
public class JiraStatisticsItem {
    
    public JiraStatisticsItem(String key, Integer count, Double sp, Double pt)
    {
        this.Key = key;
        this.Count = count;
        this.StoryPoints = sp; 
        this.Persentage = pt;
    }
    
    public String Key;
    
    public Integer Count;
    
    public Double StoryPoints;
    
    public Double Persentage;
    
    public Double getStoryPoints()
    {
        return this.StoryPoints;
    }
}
