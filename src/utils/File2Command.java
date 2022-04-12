package cn.xiaym.utils;

import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.plugins.*;

import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;
import java.util.*;

public class File2Command {
  private static boolean running = false;

  public static void run(String filePath){
    running = true;

    Path file = Paths.get(filePath);

    if(!Files.exists(file)) {
      Logger.warn("未找到脚本文件: " + filePath);
      return;
    }

    try{
      List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
      int linec = 0;
      for(String line : lines){
        if(!running) return;

        linec++;
        if(line.trim().equals("") || line.startsWith("#")) continue;

        if(isInitialCommand(line.trim())) {
          parseCommand(line.trim());
          continue;
        }

        if(!NDOSCommand.NDOSCommandParser.isVaild(line.trim())){
          Logger.err("命令不存在: (行 " + linec + ") " + line);
          return;
        } else {
          NDOSCommand.NDOSCommandParser.parse(line.trim());
          Thread.sleep(5);
        }
      }

      linec = 0;
      lines = null;
    } catch(IOException e) {
      Logger.err("错误: " + e.getMessage());
    } catch(StackOverflowError|ArrayIndexOutOfBoundsException e) {
      Logger.err("严重错误: 内存可能不足");
      Logger.err("请检查此脚本是否在不断调用其它脚本.");
      Logger.info("正在尝试释放内存..");
      System.gc();
    } catch(Exception e) {
      ErrorUtil.trace(e);
    }

    file = null;
    System.gc();
  }

  public static boolean isInitialCommand(String trimed) {
    ArrayList<String> vaildCommands = new ArrayList<>();
    vaildCommands.addAll(Arrays.asList(
          new String[]{"return", "if"}
          ));

    if(vaildCommands.contains(trimed.split(" ")[0])) return true;

    return false;
  }

  public static void parseCommand(String trimed) {
    if(trimed.toLowerCase().equals("return")) {
      Logger.debug("脚本已停止运行.");
      running = false;
      return;
    }

    if(trimed.toLowerCase().startsWith("if")) {
      boolean hasElse = false;
      boolean useEquals = false;

      ArrayList<String> args = argumentParser.parse(trimed);
      //if xxx
      if(args.size() < 5) {
        running = false;
        Logger.err("if 命令需要更多的参数");
        Logger.info("使用方法: if 表达式1 equals/noteq 表达式2 如果成功执行的命令 (else 如果失败执行的命令)");
        Logger.info("可用双引号包围某个参数");

        return;
      }

      //if xxx e/n xxx xxx else
      if(args.size() == 6) {
        running = false;
        Logger.err("else 后需要能够执行的命令.");
        return;
      }

      //判定是否有else
      if(args.size() >= 6 && "else".equals(args.get(5))) hasElse = true;

      //判定比较符为 equals 还是 noteq
      if("equals".equals(args.get(2))) useEquals = true;

      if(args.get(1).equals(args.get(3))) {
        //结果判断
        if(useEquals) {
          if(isInitialCommand(args.get(4))) {
            parseCommand(args.get(4));
          } else {
            NDOSCommand.NDOSCommandParser.parse(args.get(4));
          }
        }
      } else {
        if(!useEquals) {
          if(isInitialCommand(args.get(4))) {
            parseCommand(args.get(4));
          } else {
            NDOSCommand.NDOSCommandParser.parse(args.get(4));
          }
        }

        if(hasElse) {
          if(isInitialCommand(args.get(6))) {
            parseCommand(args.get(6));
          } else {
            NDOSCommand.NDOSCommandParser.parse(args.get(6));
          }
        }
      }

      return;
    }

  }

  /* 内置命令文档
   * return - 直接退出
   * if <表达式1> <比较符> <表达式2> <如果成功执行的命令> (else <如果失败执行的命令>)
   */
}
