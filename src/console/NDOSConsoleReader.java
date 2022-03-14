package cn.xiaym.ndos.console;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.command.*;
import cn.xiaym.ndos.*;

import org.jline.terminal.*;
import org.jline.reader.*;

public class NDOSConsoleReader implements Runnable {
  //Init
  public NDOSConsoleReader() {}

  @Override
  public void run() {
    try{
      Terminal terminal = TerminalBuilder.builder()
                          .system(true)
                          .dumb(false)
                          .encoding("UTF-8")
                          .jansi(true)
                          .jna(false)
                          .build();
      LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

      while(true) {
        String line = lineReader.readLine("");

        if(line.length() > 0) {
          NDOSCommand.NDOSCommandParser.parse(line);
        }

        Logger.flush();
      }

    } catch(UserInterruptException|EndOfFileException e) {
      System.out.println();
      System.out.flush();
      NDOSMain.exit();
    } catch(Exception e) {
      ErrorUtil.trace(e);
      System.exit(-1);
    }
  }
}
