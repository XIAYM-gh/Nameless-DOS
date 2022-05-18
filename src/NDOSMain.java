package cn.xiaym.ndos;

import cn.xiaym.ndos.console.*;
import cn.xiaym.ndos.command.*;
import cn.xiaym.utils.*;
import cn.xiaym.ndos.plugins.*;

import static cn.xiaym.utils.LanguageUtil.Lang;

import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.util.*;

public class NDOSMain {
  private static PrintStream originOut = System.out;
  private static NDOSConsoleReader reader;

  public static void main(String[] args){
    reader = new NDOSConsoleReader();

    List<String> arguments = Arrays.asList(args);
    if(arguments.contains("-debug")) NDOSAPI.DEBUG_MODE = true;

    if(ConfigUtil.get("configuration-formatting", "true").equals("true")) ConfigFormatter.doFormat();

    LanguageUtil.prepare();

    Logger.init();

    AnsiConsole.systemInstall();
    showInfo();

    ConfigUtil.init();

    Logger.info(Lang("main.loading_plugin"));
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

    System.out.flush();
    System.exit(0);
  }

  public static void escape() {
    if(NDOSAPI.COMMAND_PREFIX.equals("") && NDOSAPI.PROMPT_STRING.equals("> ")){
      exit();
      return;
    } else if(NDOSAPI.COMMAND_PREFIX.equals("") && !NDOSAPI.PROMPT_STRING.equals("> ")){
      NDOSAPI.PROMPT_STRING = "> ";
      Logger.info("[NDOS] " + Lang("main.breaking.prompt_reset"));
    } else {
      Logger.debug("[NDOS] " + Lang("main.breaking.debug.executing") + ": " + NDOSAPI.COMMAND_PREFIX + "exit");
      NDOSCommand.NDOSCommandParser.parse(NDOSAPI.COMMAND_PREFIX + "exit");
      NDOSAPI.COMMAND_PREFIX = "";
      NDOSAPI.PROMPT_STRING = "> ";
      Logger.info("[NDOS] " + Lang("main.breaking.all_reset")); 
    }

    Logger.debug("[NDOS] " + Lang("main.breaking.debug.rest"));
    new Thread(reader).start();
  }

  public static void showInfo() {
    Logger.info("Nameless DOS [" + Lang("version") + " " + NDOSAPI.NDOS_VERSION + "]");
    Logger.info("(C) 2022 Nameless Software Team " + Lang("main.serv_right"));
  }
}
