package cn.xiaym.ndos.console;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.*;

import org.jline.terminal.*;
import org.jline.reader.*;
import org.jline.reader.impl.history.*;
import org.jline.reader.impl.completer.*;

import java.nio.charset.*;

public class NDOSConsoleReader implements Runnable {

  DefaultHistory hist = new DefaultHistory();
  Boolean running;

  //Init
  public NDOSConsoleReader() {}

  @Override
  public void run() {
    try{
      running = true;

      Terminal terminal = TerminalBuilder.builder()
                          .system(true)
                          .encoding(Charset.defaultCharset())
                          .jansi(true)
                          .jna(false)
                          .build();
      
      while(running) { 
        LineReader lineReader = LineReaderBuilder.builder()
          .terminal(terminal)
          .history(hist)
          .completer(new StringsCompleter(NDOSCommand.getCompleterArray()))
          .build();

        String line = new String(lineReader.readLine().getBytes(Charset.defaultCharset()));

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
