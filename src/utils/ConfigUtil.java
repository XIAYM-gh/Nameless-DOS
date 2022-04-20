package cn.xiaym.utils;

import java.io.*;
import java.util.*;

public class ConfigUtil {
  public static synchronized void init() {
    File f = new File("config");

    if(f.exists() && !f.isDirectory()) f.delete();
    if(!f.exists()) f.mkdirs();
  }

  public static synchronized boolean hasSection(String name) {
    return new File("config/" + name + ".properties").exists();
  }

  public static xconfig getSection(String name) {
    return new xconfig("config/" + name + ".properties");
  }

  public static String get(String key) {
    return new xconfig("config.properties").get(key, null);
  }

  public static String get(String key, String def) {
    return new xconfig("config.properties").get(key, def);
  }

  public static Set<String> keySet() {
    return new xconfig("config.properties").keySet();
  }
}
