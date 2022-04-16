package cn.xiaym.ndos.console;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.*;

import org.jline.terminal.*;
import org.jline.builtins.*;
import org.jline.reader.*;
import org.jline.reader.impl.history.*;
import org.jline.reader.impl.completer.*;

import java.nio.charset.*;

public class NDOSConsoleReader implements Runnable {

  static DefaultHistory hist;
  static Boolean running;
  static Terminal terminal;
  static LineReader lineReader;
 
  //Init
  public NDOSConsoleReader() {
    try{
      TerminalBuilder terminalBuilder = TerminalBuilder.builder()
          .encoding(Charset.defaultCharset())
          .jansi(true)
          .jna(false);

      if(new xconfig("config.properties").get("use-dumb-terminal-on-windows", "true").equals("true") && System.getProperty("os.name").toLowerCase().contains("windows")) {
        Logger.info("当前正在使用Dumb Terminal, 部分快捷功能可能会被禁用.");
        terminalBuilder.dumb(true);
      }

      terminal = terminalBuilder.build();
      lineReader = LineReaderBuilder.builder().terminal(terminal).build();

      hist = new DefaultHistory();
    } catch(java.io.IOException e) {
      Logger.err("Terminal 初始化失败!");
      System.exit(-1);
    }
  }

  public static LineReader getCurrentReader() {
    return lineReader == null ? LineReaderBuilder.builder().terminal(terminal).build() : lineReader ;
  }

  @Override
  public void run() {
    try{
      running = true;
      
      while(running) { 
        lineReader = LineReaderBuilder.builder()
          .terminal(terminal)
          .history(hist)
          .completer(new ArgumentCompleter(
                new StringsCompleter(NDOSCommand.getCompleterArray()),
                new Completers.FileNameCompleter()
                ))
          .build();

        String line = new String(lineReader.readLine("\r" + NDOSAPI.PROMPT_STRING).getBytes(Charset.defaultCharset()));

        if(line.length() > 0) {
          //实现命令托管
          NDOSCommand.NDOSCommandParser.parse(NDOSAPI.COMMAND_PREFIX + line);
        }

        Logger.flush();
      }

    } catch(UserInterruptException|EndOfFileException e) {
      NDOSMain.escape();
      running = false;
    } catch(Exception e) {
      ErrorUtil.trace(e);
      running = false;
      System.exit(-1);
    }
  }
}
