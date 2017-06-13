/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.samples.akka.slacktojirabot.Engine;

import sd.samples.akka.slacktojirabot.POCO.BotConfigurationInfo;

/**
 *
 * @author sergey.d
 */
public class EngineConfiguration {
    
    private BotConfigurationInfo config;
    
    public EngineConfiguration(BotConfigurationInfo config){
        this.config = config;
    }
    
    public BotConfigurationInfo GetBotConfiguration()
    {
        return this.config;
    }
}
