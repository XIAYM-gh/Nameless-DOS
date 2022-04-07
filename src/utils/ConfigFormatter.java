package cn.xiaym.utils;

import java.util.*;

public class ConfigFormatter {
  public static void doFormat() {
    xconfig x = new xconfig();

    x.newCommentLine("Nameless DOS - Configuration");
    x.newLine("");

    x.newCommentLine("启用自动整理配置文件 (默认: true)");
    x.put("configuration-formatting", ConfigUtil.get("configuration-formatting", "true").equals("true") ? "true" : "false");

    x.newLine("");

    x.newCommentLine("在 Windows 上使用 Dumb Terminal (默认: true)");
    x.put("use-dumb-terminal-on-windows", ConfigUtil.get("use-dumb-terminal-on-windows", "true").equals("true") ? "true" : "false");

    x.newLine("");

    x.newCommentLine("其它配置");

    Set<String> set = ConfigUtil.keySet();
    set.remove("configuration-formatting");
    set.remove("use-dumb-terminal-on-windows");

    for(String key:set) {
      x.put(key, ConfigUtil.get(key, ""));
    }

    x.save("config.properties");
  }
}
