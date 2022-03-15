package cn.xiaym.ndos;

import cn.xiaym.ndos.console.*;
import cn.xiaym.utils.*;
import cn.xiaym.ndos.plugins.*;

import org.fusesource.jansi.AnsiConsole;

public class NDOSMain {
  public static void main(String[] args){
    xconfig x = new xconfig("config.properties");

    //if(!x.has("windows-force-jansi-enabled")) x.put("windows-force-jansi-enabled", "false");
    //if(!x.has("win-notification-ignored")) x.put("win-notification-ignored", "false");
    x.save();

    /*String os = System.getProperty("os.name").toLowerCase();

    if(x.get("windows-force-jansi-enabled", "false").equals("true") || !os.contains("windows")) NDOSAPI.JANSI_ENABLED = true;
    if(!NDOSAPI.JANSI_ENABLED && x.get("win-notification-ignored","false").equals("false")) {
      Logger.warn("检测到您正处于Windows环境，我们已经禁用颜色输出，如果您正在使用其他终端，请确认它可以显示ANSI颜色后在同目录的config.properties里打开 windows-force-jansi-enabled 项，此消息将不会再显示.");
      x.put("win-notification-ignored", "true");
      x.save();
    }*/

    AnsiConsole.systemInstall();

    showInfo();

    Logger.info("正在加载插件..");
    PluginMain.init();
    Logger.info("插件加载完成!");

    //开启读取线程
    new Thread(new NDOSConsoleReader()).start();
  }

  public static void exit() {
    for(JavaPlugin p : PluginMain.getPlugins()) {
      try {
        p.onDisable();
      } catch(Exception e) {
        ErrorUtil.trace(e);
      }
    }
    System.out.println();
    System.out.flush();
    System.exit(0);
  }

  public static void showInfo() {
    Logger.info("Nameless DOS [版本 "+NDOSAPI.NDOS_VERSION+"]");
    Logger.info("(C) 2022 Nameless Software Team 保留所有权利。");
  }
}
