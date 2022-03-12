package cn.xiaym.ndos.console;

import java.util.*;

public class EnvVariables {
  private static HashMap<String, String> vars = new HashMap<>();

  public static void set(String key, String value) {
    vars.put(key, value);
  }

  public static void remove(String key) {
    vars.remove(key);
  }

  public static String get(String key) {
    return vars.get(key);
  }

  public static Set<String> getVarList() {
    return vars.keySet();
  }
}
