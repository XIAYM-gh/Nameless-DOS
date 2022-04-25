package cn.xiaym.utils.script;

import cn.xiaym.utils.*;

public class globals {
  public static void runCommand(String c) {
    File2Command.runCommand(c);
  }

  public static boolean isInitialCommand(String c) {
    return File2Command.isInitialCommand(c);
  }

  public static void parseCommand(String c) {
    File2Command.parseCommand(c);
  }
}
