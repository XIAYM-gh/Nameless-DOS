package cn.xiaym.utils;

public class ErrorUtil {
  public static void trace(Exception e){
    String ExceptionName = e.toString().split(":")[0];
    Logger.err("问题 "+ExceptionName+" 描述: "+e.getMessage());
    StackTraceElement[] ste = e.getStackTrace();
    for(StackTraceElement s:ste){
      String className = s.getClassName();
      String methodName = s.getMethodName();
      String fileName = s.getFileName();
      int lineNum = s.getLineNumber();
      Logger.err("  调用类: "+className+" 调用方法: "+methodName);
      Logger.err("   - 位于文件 "+fileName+" 第 "+lineNum+" 行");
    }
  }
}
