package ndosplugin;

import cn.xiaym.ndos.plugins.*;
import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.console.*;
import cn.xiaym.ndos.*;

import cn.xiaym.utils.*;

import static cn.xiaym.utils.LanguageUtil.Lang;

import java.util.*;
import java.io.*;
import java.lang.management.*;

import static org.fusesource.jansi.Ansi.*;

public class main extends JavaPlugin {
  
  @Override public void onCommand(String cmd){
    ArrayList<String> args = argumentParser.parse(cmd);
    
    if(args.size() < 1) return;

    switch(args.get(0).toLowerCase()){
      case "exit":
        NDOSMain.exit();
        return;
      case "version":
        NDOSMain.showInfo();
        return;
      case "plugins":
        int i = 1;
        Logger.info(Lang("bp.plugins.list_title"));
        for(JavaPlugin p:PluginMain.getPlugins()) {
          String name = p.getName();
          String author = p.getAuthor();
          String version = p.getVersion();
          String id = p.getID();
          Logger.info(i + ". " + p.getName() + " (" + id + ") - v" + version + " - by " + author);
          i++;
        }
        return;
      case "help":
        NDOSCommand.showHelp(cmd);
        return;
      case "set":
        Set(cmd);
        return;
      case "echo":
        Echo(cmd);
        return;
      case "status":
        showStatus();
        return;
      case "reload":
        PluginMain.reloadPlugins();
        return;
      case "unload":
        if(args.size() < 2) return;
        PluginMain.unloadPlugin(args.get(1));
        return;
      case "load":
        if(args.size() < 2) return;
        JavaPlugin p = PluginMain.loadPlugin(new File("plugins/" + args.get(1)));

        if(p == null) {
          Logger.err(Lang("bp.load.failed_2init"));
          return;
        }

        PluginMain.executePlugin(p);

        Logger.info(Lang("bp.load.success"));
        return;
      case "clear":
        try {
          System.console().flush();
          String os = System.getProperty("os.name").toLowerCase();
          if(os.contains("windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
          } else if(os.contains("linux") || os.contains("gnu") || os.contains("freebsd") || os.contains("unix")) {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
          } else Logger.info(ansi().eraseScreen().toString());
        } catch(Exception e) {
          Logger.err(Lang("bp.clear.failure"));
          ErrorUtil.trace(e);
          return;
        }

        Logger.success(Lang("bp.clear.success"));
        return;
      case "script":
        if(args.size() < 2) {
          Logger.warn(Lang("usage", "script <文件>"));
          return;
        }

        String fileName = cmd.substring(7).trim();

        if(fileName.toLowerCase().endsWith(".java")) {
          DynamicCompiler.compile(fileName);
        } else {
          File2Command.run(fileName);
        }

        return;
      case "checkupdate":
        if(args.size() > 1 && "download".equals(args.get(1))) {
          UpdateUtil.checkUpdate(true);
          return;
        }
        UpdateUtil.checkUpdate();
        return;
      case "dir":
        String dir = "./";
        if(args.size() > 1) dir = args.get(1);

        File f = new File(dir);

        if(!f.exists() || f.isFile()) {
          Logger.err(Lang("bp.dir.not_found"));
          return;
        }

        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        ArrayList<String> output = new ArrayList<>();

        for(File sf : f.listFiles()) {
          if(sf.isFile()) {
            files.add(sf.getName());
          } else dirs.add(sf.getName());
        }

        for(String d:dirs) {
          output.add("§9" + d);
        }

        for(String file:files) {
          output.add("§r" + file);
        }

        Logger.info(Lang("bp.dir.listing", dir));

        StringBuilder sb = new StringBuilder();
        int curr = 1;
        for(String o:output) {
          sb.append(o).append(" ");

          if(curr == 5) {
            sb.append("\n");
            curr = 1;
            continue;
          }

          curr++;
        }

        Logger.info(McColorFormatter.toANSI(sb.toString()));

        return;
    }
  }

  public void Set(String cmd) {
    if(cmd.trim().equals("set")) {
      Logger.info(Lang("bp.set.list_title"));
      for(String key:EnvVariables.getVarList()){
        Logger.info(Lang("bp.set.list_child", key, EnvVariables.get(key)));
      }
      return;
    }

    ArrayList<String> args = argumentParser.parse(cmd);

    if(args.size() <= 2) {
      String a = EnvVariables.get(args.get(1));
      if(a != null) Logger.info(a);
      return;
    }

    EnvVariables.set(args.get(1), args.get(2));
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
      Logger.info(cmd.substring(5).replace("\\\"", "\""));
      return;
    }

    Logger.info(McColorFormatter.toANSI(cmd.substring(5).replace("\\\"", "\"")));
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

    Info(Lang("bp.status.title"));
    Info(Lang("bp.status.memory", headMemory.getUsed() / MB));
    Info(Lang("bp.status.thread", thread.getThreadCount()));
    Info(Lang("bp.status.class", classLoad.getLoadedClassCount(), classLoad.getUnloadedClassCount()));
    Info(Lang("bp.status.command", NDOSCommand.commandsCount()));
    Info(Lang("bp.status.plugin", PluginMain.getPlugins().size()));
  }

}
