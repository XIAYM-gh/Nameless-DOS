package cn.xiaym.utils;

import java.nio.file.*;
import java.net.*;
import java.io.*;

public class LanguageUtil {
  static ClassLoader cl = new NullClass().getClass().getClassLoader();
  static xconfig Lang = new xconfig();

  public static void prepare() {
    Path langPath = Paths.get("language.properties");
    if(!Files.exists(langPath)) {
      try {
        Files.copy(cl.getResourceAsStream("resources/language.properties"), langPath);
      } catch(Exception e) {
        Logger.err("Failed to create the language file.");
      }
    }

    Lang = new xconfig("language.properties");
  }

  public static String Lang(String key, Object... replaces) {
    prepare();

    String ret = Lang.get(key, key);

    return String.format(ret, replaces);
  }
}
