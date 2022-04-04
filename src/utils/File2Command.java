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
          Thread.sleep(5);
        }
      }

      linec = 0;
      lines = null;
    } catch(IOException e) {
      Logger.err("错误: " + e.getMessage());
    } catch(StackOverflowError|ArrayIndexOutOfBoundsException e) {
      Logger.err("严重错误: 内存可能不足");
      Logger.err("请检查此脚本是否有不停调用自己的行为");
      Logger.info("正在尝试释放内存..");
      System.gc();
    } catch(Exception e) {
      ErrorUtil.trace(e);
    }

    file = null;
    System.gc();
    }
}
