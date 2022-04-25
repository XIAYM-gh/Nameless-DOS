package cn.xiaym.utils.script;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.console.*;

import java.util.*;

public class functionBox {
  private String functionName;
  private ArrayList<String> commands;
  private HashMap<String, String> TempVars = new HashMap<>();

  public functionBox(String name, ArrayList<String> cmds) {
    this.functionName = name;
    this.commands = cmds;
  }

  public void call() {
    call(null);
  }

  public void call(ArrayList<String> args) {
    Logger.debug("F:RUN | " + functionName);

    TempVars.clear();

    //解析传入参数
    if(args != null && args.size() > 0) {
      for(int i = 0; i < args.size(); i++) {
        TempVars.put(String.valueOf(i), args.get(i));
      }
    }

    for(String line : commands) {
      for(String temp : TempVars.keySet()) {
        line = line.replace("%" + temp + "%", TempVars.getOrDefault(temp, ""));
      }

      for(String local : File2Command.getLocals().keySet()) {
        Logger.debug("Replacing: " + local + " " + File2Command.getLocal(local));
        line = line.replace("%" + local + "%", File2Command.getLocal(local));
      }

      String trimedLine = line.trim();

      if(trimedLine.startsWith("return")) return;

      if(File2Command.isInitialCommand(trimedLine)) {
        File2Command.parseCommand(trimedLine);
      } else {
        File2Command.runCommand(trimedLine);
      }
    }
  }

  public String getName() {
    return this.functionName;
  }

  public Set<String> getTempVars() {
    return this.TempVars.keySet();
  }
}
