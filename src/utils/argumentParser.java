/* 原作者: Github/1689295608 (提供 JavaScript 思路)
 * 修改者: Github/XIAYM-gh (翻译为 Java)
 *
 * 已经优化过好多次了 (
 * 速度平均在 5ms 以内
 */

package cn.xiaym.utils;

import java.util.*;

public class argumentParser {
  /* 返回一个解析完的ArrayList
   * 调用方法 argumentParser.parse(String 要解析的内容)
   */
  public static ArrayList<String> parse(String origin) {
    //如果字符串为空则返回一个空的AL
    if(origin.trim().equals("")) return new ArrayList<String>();

    //初始化部分
    String[] split = origin.split(" ");
    ArrayList<String> output = new ArrayList<>();
    Boolean merging = false;
    StringBuilder tmp = new StringBuilder();

    //解析部分
    for(String item : split) {
      //如果为 "xxx" (里面不带空格)
      if(item.startsWith("\"") && item.endsWith("\"")) {
        //这里的if是为了防止throw ArrayIndexOutOfBoundsException
        if(item.length() > 1) output.add(item.substring(1, item.length() - 1));
        continue;
      }

      if(item.startsWith("\"") && !item.startsWith("\\\"")) {
        tmp.append(item.substring(1)).append(" ");
        merging = true;
        continue;
      }

      if(item.endsWith("\"") && !item.endsWith("\\\"")) {
        tmp.append(item.substring(0, item.length() - 1));
        output.add(tmp.toString());
        tmp.delete(0, tmp.length());
        merging = false;
        continue;
      }

      if(merging) {
        tmp.append(item).append(" ");
        continue;
      }

      //不符合上述类型 返回为不带引号的xxx
      output.add(item);
    }

    return output;
  }

  public static String toArgStr(String[] args){
    String outstr = "";

    for(String s : args){
      if(!outstr.equals("")) outstr+=" ";
      if(s.contains(" ")) {
        outstr += "\""+s+"\"";
      } else {
        outstr += s;
      }
    }

    return outstr;
  }

  public static String toArgStr(ArrayList<String> args){
    String[] args_ = new String[args.size() + 1];
    for(int i=0;i<args.size();i++){
      args_[i] = args.get(i);
    }

    return toArgStr(args_);
  }

}
