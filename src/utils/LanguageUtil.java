package cn.xiaym.utils;

import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class LanguageUtil {
  static ClassLoader cl = new NullClass().getClass().getClassLoader();
  static xconfig Lang = new xconfig();
  static Properties def = new Properties();

  static {
    try {
      def.load(new InputStreamReader(
            cl.getResourceAsStream("resources/language.properties"),
            Charset.defaultCharset()
            ));

    } catch(IOException e) {}
  }

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

    String ret = Lang.get(key, def.getProperty(key, key));

    return String.format(ret, replaces).replace("\\t", "\t").replace("\\n", "\n");
  }
}
