package cn.xiaym.utils.script;

import static cn.xiaym.utils.LanguageUtil.Lang;
import static cn.xiaym.utils.script.globals.*;
import cn.xiaym.utils.*;
import cn.xiaym.ndos.console.*;

import java.util.*;

public class If {
  public static void If(ArrayList<String> args, HashMap<String, functionBox> fList, String current_fun_name, boolean in_function) {
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
          File2Command.stop();
          return;
      }

      if(useIsset) di--;

      //if xxx
      if(args.size() < di) {
        File2Command.stop();
        Logger.err(Lang("script.if.more_var"));

        return;
      }

      //if xxx e/n xxx xxx else
      if(args.size() == (di + 1)) {
        File2Command.stop();
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
