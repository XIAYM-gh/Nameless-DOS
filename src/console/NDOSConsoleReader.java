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
                          .encoding("UTF-8")
                          .jansi(true)
                          .jna(false)
                          .build();
      LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

      while(true) {
        String line = new String(lineReader.readLine("").getBytes("UTF-8"));

        if(line.length() > 0) {
          //实现命令托管
          NDOSCommand.NDOSCommandParser.parse(NDOSAPI.COMMAND_PREFIX + line);
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
