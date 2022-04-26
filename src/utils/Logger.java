package cn.xiaym.utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

import cn.xiaym.ndos.*;
import cn.xiaym.ndos.console.*;

import org.fusesource.jansi.*;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public final class Logger {
  private static Boolean inited = false;
  private static Console console = System.console();

  public static void init() {
    if(inited) return;

    System.setOut(new outObserver("STDOUT"));
    System.setErr(new outObserver("STDERR"));

    inited = true;
  }

  private static void out(String str, Level level, String typecolor, String textcolor){
    String[] str_split = str.split("\n");

    for(String str_:str_split){
      String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

      StringBuilder b = new StringBuilder("\r");

      //时间
      b.append(time);

      b.append(" ");

      //等级
      b.append("[").append(ansi().fgBright(ConvertColor(typecolor)).bold().a(level.getFlag()).reset()).append("]");

      b.append(" ");

      //输出内容
      b.append(ansi().fgBright(ConvertColor(textcolor)).a(str_).reset());

      AnsiConsole.out().println(b.toString());
    }

    Logger.flush();
  }

  public static void info(Object obj){
    Logger.out(String.valueOf(obj), Level.INFO, "default", "default");
  }

  public static void warn(Object obj){
    Logger.out(String.valueOf(obj), Level.WARN, "yellow", "yellow");
  }

  public static void err(Object obj){
    Logger.out(String.valueOf(obj), Level.ERROR, "red", "red");
  }

  public static void success(Object obj){
    Logger.out(String.valueOf(obj), Level.INFO, "default", "green");
  }

  public static void debug(Object obj){
    if(NDOSAPI.DEBUG_MODE) Logger.out(String.valueOf(obj), Level.DEBUG, "blue", "default");
  }

  public static void flush(){
    AnsiConsole.out().print("\r" + NDOSAPI.PROMPT_STRING);
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

  public static enum Level {
    DEBUG("D"),
    INFO("I"),
    WARN("W"),
    ERROR("E");

    private String flag;

    private Level(String flag) {
      this.flag = flag;
    }

    public String getFlag() {
      return flag;
    }
  }

  private final static class outObserver extends PrintStream {
    private String c;

    public outObserver(String channelName) {
      super(new ByteArrayOutputStream());
      this.c = channelName;
    }

    public void print(String o) { println(o); }
    public void print(Integer o) { println(o); }
    public void print(Character o) { println(o); }
    public void print(Long o) { println(o); }
    public void print(Boolean o) { println(o); }
    public void print(Float o) { println(o); }
    public void print(Double o) { println(o); }
    public void print(Object o) { println(o); }

    public void println(String o) { Logger.info(p(o)); }
    public void println(Integer o) { Logger.info(p(o)); }
    public void println(Character o) { Logger.info(p(o)); }
    public void println(Long o) { Logger.info(p(o)); }
    public void println(Boolean o) { Logger.info(p(o)); }
    public void println(Float o) { Logger.info(p(o)); }
    public void println(Double o) { Logger.info(p(o)); }
    public void println(Object o) { Logger.info(p(o)); }

    private String p(Object o) {
      return "[" + c + "] " + o;
    }
  }
}
