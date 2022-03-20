package cn.xiaym.utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import cn.xiaym.ndos.*;

import org.fusesource.jansi.*;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public class Logger {
  public static void out(String str, String type, String typecolor, String textcolor){
    String[] str_split = str.split("\n");
    for(String str_:str_split){
      String time = new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));

      /*if(!NDOSAPI.JANSI_ENABLED) {
        String REGEX_STR = "\\x1b(\\[.*?[@-~]|\\].*?(\\x07|\\x1b\\\\))";
        AnsiConsole.out().println("\r["+time+" "+type+"] "+str.replaceAll(REGEX_STR, ""));
        return;
      }*/

      AnsiConsole.out().println("\r"+time+" ["+ansi().fgBright(ConvertColor(typecolor)).bold().a(type).reset()+"] "+ansi().fgBright(ConvertColor(textcolor)).a(str_).reset());
    }

    Logger.flush();
  }

  public static void info(Object obj){
    Logger.out(String.valueOf(obj), "I", "default", "default");
  }

  public static void warn(Object obj){
    Logger.out(String.valueOf(obj), "W", "yellow", "yellow");
  }

  public static void err(Object obj){
    Logger.out(String.valueOf(obj), "E", "red", "red");
  }

  public static void success(Object obj){
    Logger.out(String.valueOf(obj), "I", "default", "green");
  }

  public static void flush(){
    System.out.flush();
    System.err.flush();
    System.out.print("\r" + NDOSAPI.PROMPT_STRING);
  }

  public static Color ConvertColor(String colorName){
    switch(colorName.toLowerCase()){
      case "red":
        return RED;
      case "blue":
        return BLUE;
      case "purple":
      case "magenta":
        return MAGENTA;
      case "black":
        return BLACK;
      case "white":
        return WHITE;
      case "yellow":
        return YELLOW;
      case "cyan":
      case "grey":
        return CYAN;
      case "green":
        return GREEN;
      case "default":
        return DEFAULT;
      default:
        return DEFAULT;
    }
  }

  public static void Test(){
    Logger.Test("default");
    Logger.Test("red");
    Logger.Test("blue");
    Logger.Test("purple");
    Logger.Test("black");
    Logger.Test("white");
    Logger.Test("yellow");
    Logger.Test("cyan");
    Logger.Test("green");
  }

  public static void Test(String color){
    Logger.out(color + " test", "TESTING", "blue", color);
  }

  public static void outByRender(String r){
    System.out.println(ansi().render(r));
  }
}
