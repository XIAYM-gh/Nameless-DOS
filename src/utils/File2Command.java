package cn.xiaym.utils;

import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.plugins.*;
import cn.xiaym.ndos.console.*;

import static cn.xiaym.utils.LanguageUtil.Lang;

import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;
import java.util.*;

public class File2Command {
  private static boolean running = false;
  
  static boolean in_function = false;
  static String current_fun_name = "";
  private static HashMap<String, FunctionBox> fList = new HashMap<>();
  
  public static void run(String filePath) {
    running = true;
    in_function = false;

    importScript(filePath);
  }

  private static void importScript(String filePath) {
    boolean isParsingFunction = false;
    String fN = "";
    ArrayList<String> fCmd = new ArrayList<>();
    fList = new HashMap<>();

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
            fList.put(fN, new FunctionBox(fN, fCmd));

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
      Logger.err(Lang("script.oomerr").replace("\\n", "\n"));
      Logger.info(Lang("script.gc"));
      System.gc();
    } catch(Exception e) {
      ErrorUtil.trace(e);
    }

    file = null;
    System.gc();
  }

  public static void runCommand(String cmd) {
    if(NDOSCommand.NDOSCommandParser.isVaild(cmd)) {
      NDOSCommand.NDOSCommandParser.parse(cmd);
    } else {
      Logger.err(Lang("script.not_command", cmd));
    }
  }

  public static boolean isInitialCommand(String trimed) {
    ArrayList<String> vaildCommands = new ArrayList<>();
    vaildCommands.addAll(Arrays.asList(
          new String[]{"return", "if", 
            "stop_script", "run_command",
            "import"}
          ));

    if(vaildCommands.contains(trimed.split(" ")[0])) return true;

    return false;
  }

  public static void parseCommand(String trimed) {
    ArrayList<String> args = argumentParser.parse(trimed);

    if("return".equals(args.get(0)) || "stop_script".equals(args.get(0))) {
      Logger.debug(Lang("script.debug.stopped"));
      running = false;
      return;
    }

    if("import".equals(args.get(0))) {
      if(args.size() >= 2) {
        importScript(args.get(1));
      } else {
        Logger.err(Lang("script.import_file_required"));
      }
      return;
    }

    if("run_command".equals(args.get(0))) {
      runCommand(trimed.substring(11).trim());
      return;
    }

    if("if".equals(args.get(0))) {
      String mode;

      boolean hasElse = false;
      boolean useCompare = false;
      boolean useIsset = false;

      int di = 5;

      //判定比较符
      switch(args.get(2).toLowerCase()) {
        case "equals":
          mode = "equals";
          useCompare = true;
          break;
        case "noteq":
          mode = "noteq";
          useCompare = true;
          break;
        case "isset":
          mode = "isset";
          useIsset = true;
          break;
        case "notset":
          mode = "notset";
          useIsset = true;
          break;
        default:
          Logger.err(Lang("script.if.not_match"));
          Logger.err("equals, noteq, isset, notset");
          running = false;
          return;
      }

      if(useIsset) di--;

      //if xxx
      if(args.size() < di) {
        running = false;
        Logger.err(Lang("script.if.more_var"));

        return;
      }

      //if xxx e/n xxx xxx else
      if(args.size() == (di + 1)) {
        running = false;
        Logger.err(Lang("script.if.require_cmd"));
        return;
      }

      //判定是否有else
      if(args.size() >= (di + 1) && "else".equals(args.get(di))) hasElse = true;

      if(useCompare){
        boolean useEquals = "equals".equals(mode);

        if(args.get(1).equals(args.get(di-2))) {
          //结果判断
          if(useEquals) {
            if(isInitialCommand(args.get(4))) {
              parseCommand(args.get(4));
            } else runCommand(args.get(4));
          }
        } else {
          if(!useEquals) {
            if(isInitialCommand(args.get(4))) {
              parseCommand(args.get(4));
            } else runCommand(args.get(4));
          }

          if(hasElse) {
            if(isInitialCommand(args.get(6))) {
              parseCommand(args.get(6));
            } else runCommand(args.get(6));
          }
        }

        return;
      }

      //if var_name isset/notset do else do
      if(useIsset) {
        boolean useIsMode = "isset".equals(mode);

        ArrayList<String> argList = new ArrayList<>();
        if(in_function) argList.addAll(fList.get(current_fun_name).getTempVars());
        argList.addAll(EnvVariables.getVarList());

        if(useIsMode) {
          if(argList.contains(args.get(1))) {
            if(isInitialCommand(args.get(3))) {
              parseCommand(args.get(3));
            } else runCommand(args.get(3));
          } else if(hasElse) {
            if(isInitialCommand(args.get(5))) {
              parseCommand(args.get(5));
            } else runCommand(args.get(5));
          }
        } else {
          //NOTSET
          if(!argList.contains(args.get(1))) {
            if(isInitialCommand(args.get(3))) {
              parseCommand(args.get(3));
            } else runCommand(args.get(3));
          } else if(hasElse) {
            if(isInitialCommand(args.get(5))) {
              parseCommand(args.get(5));
            } else runCommand(args.get(5));
          }
        }
      }

      return;
    }

  }

  /* IF 可使用的句型
   * if var1 equals var2
   * if var1 noteq var2
   * if var1 isset
   * if var1 notset
   */
}

class FunctionBox {
  private String functionName;
  private ArrayList<String> commands;
  private HashMap<String, String> TempVars = new HashMap<>();

  public FunctionBox(String name, ArrayList<String> cmds) {
    this.functionName = name;
    this.commands = cmds;
  }

  public void call() {
    call(null);
  }

  public void call(ArrayList<String> args) {
    Logger.debug("F:RUN | " + functionName);

    TempVars.clear();

    //解析传入参数
    if(args != null && args.size() > 0) {
      for(int i = 0; i < args.size(); i++) {
        TempVars.put(String.valueOf(i), args.get(i));
      }
    }

    for(String line : commands) {
      for(String temp : TempVars.keySet()) {
        line = line.replace("%" + temp + "%", TempVars.getOrDefault(temp, ""));
      }

      String trimedLine = line.trim();

      if(trimedLine.startsWith("return")) return;

      if(File2Command.isInitialCommand(trimedLine)) {
        File2Command.parseCommand(trimedLine);
      } else {
        File2Command.runCommand(trimedLine);
      }
    }
  }

  public String getName() {
    return this.functionName;
  }

  public Set<String> getTempVars() {
    return this.TempVars.keySet();
  }
}
