package cn.xiaym.ndos;

import cn.xiaym.ndos.console.*;
import cn.xiaym.utils.*;
import cn.xiaym.ndos.plugins.*;

import org.fusesource.jansi.AnsiConsole;

public class NDOSMain {
  public static void main(String[] args){
    xconfig x = new xconfig("config.properties");

    if(x.get("configuration-formatting", "true").equals("true")) ConfigFormatter.doFormat();

    AnsiConsole.systemInstall();

    showInfo();

    Logger.info("正在加载插件..");
    PluginMain.init(false);
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
