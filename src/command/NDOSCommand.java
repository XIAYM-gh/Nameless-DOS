package cn.xiaym.ndos.command;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.plugins.*;
import cn.xiaym.ndos.console.*;

import java.util.*;

import org.json.*;

//MAIN
public class NDOSCommand {
  private static HashMap<String, String> RegisteredCommands = new HashMap<>();
  private static HashMap<String, String> CommandUsage = new HashMap<>();
  private static HashMap<String, String> CommandTips = new HashMap<>();
  private static HashMap<String, String> RegExecutors = new HashMap<>();

  private static void registerCommand(String cmd, String desc) {
    if(cmd != null) RegisteredCommands.put(cmd.toLowerCase(), desc);
  }

  public static void registerCommand(String cmd, String desc, String pluginID) {
    registerCommand(cmd, desc);
    if(cmd != null && pluginID != null) RegExecutors.put(cmd.toLowerCase(), pluginID);
  }

  public static void registerUsage(String cmd, String usage) {
    if(usage != null && cmd != null) CommandUsage.put(cmd.toLowerCase(), usage);
  }

  public static void registerTip(String cmd, String tip) {
    if(tip != null && cmd != null) CommandTips.put(cmd.toLowerCase(), tip);
  }

  public static void deleteCommand(String cmd) {
    cmd = cmd.toLowerCase();

    RegisteredCommands.remove(cmd);
    CommandUsage.remove(cmd);
    CommandTips.remove(cmd);
    RegExecutors.remove(cmd);
  }

  public static void removeByPlugin(JavaPlugin p) {
    String pid = p.getID();
    ArrayList<String> rl = new ArrayList<>();
    for(String ckey : RegExecutors.keySet()) {
      if(RegExecutors.get(ckey).equals(pid)) {
        rl.add(ckey);
      }
    }

    for(String ckey : rl) {
      deleteCommand(ckey);
    }
  }

  //插件无法调用此方法
  public static void clearAllCommands() {
    if(PluginMain.isPluginClass(new Throwable().getStackTrace()[1].getClassName())) return;

    RegisteredCommands.clear();
    CommandUsage.clear();
    CommandTips.clear();
    RegExecutors.clear();

    Logger.debug("已经清除所有命令.");
  }

  public static int commandsCount() {
    return RegisteredCommands.size();
  }

  public static void processPlugin(Properties pc, String pluginID) {
    try{
      JSONArray ja = new JSONArray(pc.getProperty("plugin.commands", "[]"));
      for (int i = 0; i < ja.length(); i++) {
        String cmdid = ja.getString(i);
        String cmdRegisterName = pc.getProperty("cmd."+cmdid+".reg-name", null);
        String cmdDesc = pc.getProperty("cmd."+cmdid+".desc", "A Command.");
        registerCommand(cmdRegisterName, cmdDesc);

        String cmdUsage = pc.getProperty("cmd."+cmdid+".usage", null);
        registerUsage(cmdRegisterName, cmdUsage);

        String cmdTip = pc.getProperty("cmd."+cmdid+".tips", null);
        registerTip(cmdRegisterName, cmdTip);

        if(cmdRegisterName != null && pluginID != null) RegExecutors.put(cmdRegisterName.toLowerCase(), pluginID);
      }
    } catch(Exception e) {
      Logger.err("无法添加命令: "+e.getMessage());
    }
  }

  public static void showHelp(String cmd) {
    cmd = cmd.toLowerCase();

    Boolean havepage = false;
    int page = 1;
    if(cmd.length() > 5 && is_int(cmd.substring(5))) {
      havepage = true;
      page = Integer.parseInt(cmd.substring(5));
    }

    int pageCount = (int) Math.floor(RegisteredCommands.size() /  10) + 1;

    if(cmd.trim().equals("help") || havepage) {
      Info("§6====== 命令帮助 (第 " + page + "/" + pageCount + " 页) ======");
      if(page < 0 || page > pageCount) return;

      Map<String, String> sorted = new TreeMap<>(RegisteredCommands);

      for(int cou=(page-1)*10; (cou < page*10 && cou < sorted.size()); cou++) {
        String seckey = (String) sorted.keySet().toArray()[cou];
        Info("§9" + seckey + "§r - §f" + RegisteredCommands.get(seckey));
      }
    } else {
      String cmdName = cmd.substring(5).toLowerCase();
      if(RegisteredCommands.containsKey(cmdName)){
        Info("§6====== 命令 "+cmdName+" 的帮助 ======");
        Info("§f" + RegisteredCommands.get(cmdName) + "\n");
        if(CommandTips.containsKey(cmdName)) Info("§f提示:\n§d" + CommandTips.get(cmdName));
        if(CommandUsage.containsKey(cmdName)) Info("§f用法:\n§d" + CommandUsage.get(cmdName));
      } else {
        Logger.warn("已注册的命令列表中不存在此项.");
      }
    }
  }

  public static boolean is_int(String origin) {
    try {
      Integer.parseInt(origin);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public static void Info(String msg) {
    Logger.info(McColorFormatter.toANSI(msg));
  }

  public static Set<String> getCompleterArray() {
    return new TreeMap<>(RegisteredCommands).keySet();
  }

  //sub-class Parser
  public class NDOSCommandParser {
    public static void parse(String cmd) {
      for (String vkey : EnvVariables.getVarList()) {
        cmd = cmd.replaceAll("%" + vkey + "%", EnvVariables.get(vkey));
      }

      if(isVaild(cmd.toLowerCase())){
        JavaPlugin executor = PluginMain.getPlugin(RegExecutors.get(cmd.split(" ")[0].toLowerCase()));
        if(executor != null){
          try {
            executor.onCommand(cmd);
          } catch(Exception e) {
            Logger.err("运行命令时发生非预期错误");
            ErrorUtil.trace(e);
          }
        } else {
          Logger.err("无法找到能够执行此命令的插件，(如果可能)请联系插件作者");
        }
      } else {
        Logger.warn("未知命令.");
      }
    }

    public static boolean isVaild(String cmd) {
      for(String c : RegisteredCommands.keySet()) {
        if(cmd.equals(c) || cmd.startsWith(c + " ")) {
          return true;
        }
      }

      return false;
    }
  }
}
