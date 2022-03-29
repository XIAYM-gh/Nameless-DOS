package cn.xiaym.utils;

import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.plugins.*;

import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;
import java.util.*;

public class File2Command {
  public static void run(String filePath){
    Path file = Paths.get(filePath);

    if(!Files.exists(file)) {
      Logger.warn("未找到脚本文件: " + filePath);
      return;
    }

    try{
      List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
      int linec = 0;
      for(String line : lines){
        linec++;
        if(line.trim().equals("") || line.startsWith("#")) continue;
        if(!NDOSCommand.NDOSCommandParser.isVaild(line)){
          Logger.err("命令不存在: (行 " + linec + ") " + line);
          return;
        } else {
          NDOSCommand.NDOSCommandParser.parse(line);
        }
      }
    } catch(IOException e) {
      Logger.err("错误: " + e.getMessage());
    }
  }
}
