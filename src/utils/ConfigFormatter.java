package cn.xiaym.utils;

import java.util.*;

public class ConfigFormatter {
  public static void doFormat() {
    xconfig x = new xconfig("config.properties");
    xconfig xNew = new xconfig();

    xNew.newCommentLine("Nameless DOS - Configuration");
    xNew.newLine("");

    xNew.newCommentLine("启用自动整理配置文件 (默认: true)");
    xNew.put("configuration-formatting", x.get("configuration-formatting", "true").equals("true") ? "true" : "false");

    xNew.newLine("");

    xNew.newCommentLine("在 Windows 上使用 Dumb Terminal (默认: true)");
    xNew.put("use-dumb-terminal-on-windows", x.get("use-dumb-terminal-on-windows", "true").equals("true") ? "true" : "false");

    xNew.newLine("");

    xNew.newCommentLine("其它配置");

    Set<String> set = xNew.keySet();
    set.remove("configuration-formatting");
    set.remove("use-dumb-terminal-on-windows");

    for(String key:set) {
      xNew.put(key, x.get(key, ""));
    }

    xNew.save("config.properties");
  }
}
