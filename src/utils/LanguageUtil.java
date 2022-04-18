package cn.xiaym.utils;

import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class LanguageUtil {
  static Path langPath = Paths.get("language.properties");
  static ClassLoader cl = new NullClass().getClass().getClassLoader();
  static xconfig Lang = new xconfig();
  static xconfig def = new xconfig();

  static {
    try {
      //Prepare Default Language Properties
      Path tempPath = Paths.get(".defaultLang.tmp");
      Files.copy(cl.getResourceAsStream("resources/language.properties"), tempPath, StandardCopyOption.REPLACE_EXISTING);
      def = new xconfig(".defaultLang.tmp");
      Files.delete(tempPath);
    } catch(IOException e) {}
  }

  public static void prepare() {
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

    String ret = Lang.get(key, def.get(key, key));

    return String.format(ret, replaces).replace("\\t", "\t").replace("\\n", "\n");
  }
}
