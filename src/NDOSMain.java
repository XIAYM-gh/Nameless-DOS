package cn.xiaym.ndos;

import cn.xiaym.ndos.console.*;
import cn.xiaym.ndos.command.*;
import cn.xiaym.utils.*;
import cn.xiaym.ndos.plugins.*;

import org.fusesource.jansi.AnsiConsole;

import java.util.*;

public class NDOSMain {
  private static NDOSConsoleReader reader = new NDOSConsoleReader();

  public static void main(String[] args){

    List<String> arguments = Arrays.asList(args);
    if(arguments.contains("-debug")) NDOSAPI.DEBUG_MODE = true;

    xconfig x = new xconfig("config.properties");

    if(x.get("configuration-formatting", "true").equals("true")) ConfigFormatter.doFormat();

    AnsiConsole.systemInstall();

    showInfo();

    Logger.info("正在加载插件..");
    PluginMain.init(false);

    //开启读取线程
    new Thread(reader).start();
  }

  public static void exit() {
    for(JavaPlugin p : PluginMain.getPlugins()) {
      try {
        p.onDisable();
      } catch(Exception e) {
        ErrorUtil.trace(e);
      }
    }

    System.out.println("\n");
    System.out.flush();
    System.exit(0);
  }

  public static void escape() {
    if(NDOSAPI.COMMAND_PREFIX.equals("") && NDOSAPI.PROMPT_STRING.equals("> ")){
      exit();
      return;
    } else if(NDOSAPI.COMMAND_PREFIX.equals("") && !NDOSAPI.PROMPT_STRING.equals("> ")){
      NDOSAPI.PROMPT_STRING = "> ";
      Logger.info("[NDOS] 已经重置输入提示符，再次按下 ctrl + c/d 或输入 exit 退出.");
    } else {
      Logger.debug("[NDOS] 正在尝试执行命令: " + NDOSAPI.COMMAND_PREFIX + "exit");
      NDOSCommand.NDOSCommandParser.parse(NDOSAPI.COMMAND_PREFIX + "exit");
      NDOSAPI.COMMAND_PREFIX = "";
      NDOSAPI.PROMPT_STRING = "> ";
      Logger.info("[NDOS] 已经重置提示符状态，如果插件发生问题请重启 NDOS."); 
    }

    Logger.debug("[NDOS] 正在重启命令读取线程..");
    new Thread(reader).start();
  }

  public static void showInfo() {
    Logger.info("Nameless DOS [版本 " + NDOSAPI.NDOS_VERSION + "]");
    Logger.info("(C) 2022 Nameless Software Team 保留所有权利。");
  }
}
