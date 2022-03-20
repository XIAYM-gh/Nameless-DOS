package ndosplugin;

import cn.xiaym.ndos.plugins.*;
import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.console.*;
import cn.xiaym.ndos.*;

import cn.xiaym.utils.*;

import java.util.*;
import java.lang.management.*;

import static org.fusesource.jansi.Ansi.*;

public class main extends JavaPlugin {
  
  @Override public void onCommand(String cmd){
    String cmd_ = cmd.toLowerCase();

    if(cmd_.startsWith("exit")) {
      NDOSMain.exit();
    } else if(cmd_.startsWith("version")) {
      NDOSMain.showInfo();
    } else if(cmd_.startsWith("plugins")) {
      int i = 1;
      Logger.info("====== 插件列表 ======");
      for(JavaPlugin p:PluginMain.getPlugins()) {
        String name = p.getName();
        String author = p.getAuthor();
        String version = p.getVersion();
        String id = p.getID();
        Logger.info(i + ". " + p.getName() + " (" + id + ") - v" + version + " - by " + author);
        i++;
      }
    } else if(cmd_.startsWith("help")) {
      NDOSCommand.showHelp(cmd);
    } else if(cmd_.startsWith("set")) {
      Set(cmd);
    } else if(cmd_.startsWith("echo")) {
      Echo(cmd);
    } else if(cmd_.startsWith("status")) {
      showStatus();
    } else if(cmd_.startsWith("clear")) {
      try {
        System.console().flush();
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("windows")) {
          new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else if(os.contains("linux")) {
          new ProcessBuilder("clear").inheritIO().start().waitFor();
        } else Logger.info(ansi().eraseScreen().toString());
      } catch(Exception e) {
        Logger.err("清除屏幕失败!");
        ErrorUtil.trace(e);
      }

      Logger.success("清除屏幕成功!");
    }
  }

  public void Set(String cmd) {
    if(cmd.trim().equals("set")) {
      Logger.info("变量列表:");
      for(String key:EnvVariables.getVarList()){
        Logger.info(key + " = " + EnvVariables.get(key));
      }
      return;
    }

    String arg = cmd.substring(4);
    if(!arg.contains("=")) {
      String a = EnvVariables.get(arg);
      if(a != null) Logger.info(a);
      return;
    }

    if(arg.equals("=") || arg.endsWith("=") || arg.startsWith("=")) return;

    String[] args = arg.split("=");
    if(args[0].equals("") || args[1].equals("")) return;

    EnvVariables.set(args[0], arg.substring(args[0].length() + 1));
  }

  public void Echo(String cmd) {
    boolean useFormatter = false;
    if(cmd.trim().equals("echo")) {
      Logger.info("");
      return;
    }

    List<String> args = Arrays.asList(cmd.split(" "));

    if(args.contains("--format")) {
      cmd = cmd.replaceFirst(" --format", "");
      useFormatter = true;
    }

    if(!useFormatter) {
      Logger.info(cmd.substring(5));
      return;
    }

    Logger.info(McColorFormatter.toANSI(cmd.substring(5)));
  }

  public void Info(String msg) {
    Logger.info(McColorFormatter.toANSI(msg));
  }

  public void showStatus() {
    long MB = 1024 * 1024;
    
    ClassLoadingMXBean classLoad = ManagementFactory.getClassLoadingMXBean();

    MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    MemoryUsage headMemory = memory.getHeapMemoryUsage();

    ThreadMXBean thread = ManagementFactory.getThreadMXBean();

    Info("§6 ====== NDOS 状态 ======");
    Info("§a已用内存: \t\t§e" + headMemory.getUsed() / MB + " MB");
    Info("§a当前线程数: \t§e" + thread.getThreadCount()); 
    Info("§a已加载类总数: \t§e" + classLoad.getLoadedClassCount() + " §f(" + classLoad.getUnloadedClassCount() + " 个类已卸载)");
    Info("§a已注册命令数: \t§e" + NDOSCommand.commandsCount());
    Info("§a已加载插件数: \t§e" + PluginMain.getPlugins().size());
  }

}
