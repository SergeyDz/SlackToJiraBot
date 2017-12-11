/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd.bot.akka.slacktojirabot.Engine;

/**
 *
 * @author sergey.d
 */
public class BotEngineShutdown {
  
  public String Message;
    
  public  BotEngineShutdown(String message)
  {
      this.Message = message;
  }
}
