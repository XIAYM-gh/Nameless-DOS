package cn.xiaym.utils;

import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.plugins.*;
import cn.xiaym.ndos.console.*;
import cn.xiaym.utils.script.*;

import static cn.xiaym.utils.LanguageUtil.Lang;

import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;
import java.util.*;

public class File2Command {
  private static boolean running = false;
  
  static boolean in_function = false;
  static String current_fun_name = "";
  private static HashMap<String, functionBox> fList = new HashMap<>();
  private static HashMap<String, String> locals = new HashMap<>();
  
  public static void run(String filePath) {
    running = true;
    in_function = false;

    importScript(filePath);
  }

  private static void importScript(String filePath) {
    boolean isParsingFunction = false;
    String fN = "";
    ArrayList<String> fCmd = new ArrayList<>();
    fList.clear();
    locals.clear();

    Path file = Paths.get(filePath);

    if(!Files.exists(file)) {
      Logger.warn(Lang("script.not_found", filePath));
      return;
    }

    try{
      List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
      int linec = 0;
      for(String line : lines){
        if(!running) return;

        linec++;
        String trimedLine = line.trim();
        trimedLine = replaceVar(trimedLine);

        if(trimedLine.endsWith(";")) {
          trimedLine = trimedLine.substring(0, trimedLine.length() - 1);
        }

        if(trimedLine.equals("") || trimedLine.startsWith("#")) continue;

        //尝试解析function
        if(!isParsingFunction) {
          if(trimedLine.startsWith("function") && trimedLine.endsWith("{")) {
            String fn = trimedLine.substring(8, trimedLine.length()-1);
            if(fn.trim().equals("")) {
              Logger.err(Lang("script.function_name_required"));
              return;
            }

            fN = fn.trim();

            isParsingFunction = true;
            Logger.debug("F:START | " + trimedLine);
            continue;
          }
        }
        
        if(isParsingFunction) {
          //END
          if(trimedLine.equals("}")) {
            fList.put(fN, new functionBox(fN, fCmd));

            fN = "";
            fCmd = new ArrayList<>();
            isParsingFunction = false;
            Logger.debug("F:STOP | " + trimedLine);
            continue;
          }

          fCmd.add(trimedLine);
          Logger.debug("F:ADD | " + trimedLine);
          continue;
        }

        //如果是function
        ArrayList<String> args = argumentParser.parse(trimedLine);
        
        if(fList.containsKey(args.get(0))) {
          ArrayList<String> fArgs = new ArrayList<String>();
          fArgs.addAll(args);
          fArgs.remove(0);

          in_function = true;
          current_fun_name = args.get(0);
          fList.get(args.get(0)).call(fArgs);
          Logger.debug("F:DONE " + args.get(0) + " | " + trimedLine);
          in_function = false;
          current_fun_name = "";

          continue;
        }

        if(isInitialCommand(trimedLine)) {
          parseCommand(trimedLine);
          continue;
        }

        if(!NDOSCommand.NDOSCommandParser.isVaild(trimedLine)){
          Logger.err(Lang("script.line_traced", linec, line));
          return;
        } else {
          runCommand(trimedLine);
        }
      }

      linec = 0;
      lines = null;
    } catch(IOException e) {
      Logger.err("错误: " + e.getMessage());
    } catch(StackOverflowError|ArrayIndexOutOfBoundsException e) {
      Logger.err(Lang("script.oomerr"));
      Logger.info(Lang("script.gc"));
      System.gc();
    } catch(Exception e) {
      ErrorUtil.trace(e);
    }

    file = null;
    System.gc();
  }

  public static String replaceVar(String raw) {
    for(String env : EnvVariables.getVarList()) {
      raw = raw.replace("%" + env + "%", EnvVariables.get(env, ""));
    }

    for(String local : locals.keySet()) {
      Logger.debug("Replacing: " + local + " " + getLocal(local));
      raw = raw.replace("%" + local + "%", getLocal(local));
    }

    return raw;
  }

  public static void runCommand(String cmd) {
    if(NDOSCommand.NDOSCommandParser.isVaild(cmd)) {
      NDOSCommand.NDOSCommandParser.parse(cmd);
    } else {
      Logger.err(Lang("script.not_command", cmd));
    }
  }

  public static boolean isInitialCommand(String trimed) {
    List<String> vaildCommands = Arrays.asList(
          new String[]{"return", "if", 
            "stop_script", "run_command",
            "import", "local"}
          );

    if(vaildCommands.contains(trimed.split(" ")[0])) return true;

    return false;
  }

  public static void parseCommand(String trimed) {
    ArrayList<String> args = argumentParser.parse(trimed);

    if(args.size() < 1) return;

    switch(args.get(0)){
      case "return":
      case "stop_script":
        Logger.debug(Lang("script.debug.stopped"));
        running = false;
        return;
      case "import":
        if(args.size() >= 2) {
          importScript(args.get(1));
        } else Logger.err(Lang("script.import_file_required"));
        return;
      case "local":
        if(args.size() < 3) return;

        if("null".equals(args.get(2))) {
          locals.remove(args.get(1));
          return;
        }

        locals.put(args.get(1), args.get(2));
        return;
      case "run_command":
        runCommand(trimed.substring(11).trim());
        return;
      case "if":
        If.If(args, fList, current_fun_name, in_function, locals);
        return;
    }
  }

  public static void stop() {
    running = false;
  }

  public static HashMap<String, String> getLocals() {
    return locals;
  }

  public static String getLocal(String key) {
    return locals.getOrDefault(key, "");
  }
}
