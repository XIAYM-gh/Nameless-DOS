package cn.xiaym.utils;

import org.glamey.compiler.*;

import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class DynamicCompiler {
  public static void compile(String fileName) {
    String ClassName = fileName.trim().substring(0, fileName.trim().length() - 5); //Remove .java
    Path f = Paths.get(fileName);
    
    if(!Files.exists(f)) {
      Logger.err("您指定的 Java 文件不存在.");
      return;
    }

    DynamicJavaCompiler comp = new DynamicJavaCompiler(new NullClass().getClass().getClassLoader());

    List<String> contentList;

    try {
      contentList = Files.readAllLines(f);
    } catch(IOException e) {
      Logger.err("无法读取指定文件内容.");
      return;
    }

    String sourceContent = String.join("", contentList);
    comp.addSource(ClassName, sourceContent);

    try {
      Map<String, Class<?>> classMap = comp.genClasses();

      Class<?> compiledClass = (Class<?>) classMap.values().toArray()[0];;

      if(compiledClass == null) {
        Logger.err("无法运行此 Class: 编译后的字节码为空.");
        return;
      }

      Method m = compiledClass.getDeclaredMethod("main");

      if (m == null) {
        Logger.err("此 Java 脚本不包含无参 main 方法，运行失败.");
        return;
      }

      m.setAccessible(true);
      m.invoke(compiledClass.getDeclaredConstructor().newInstance());
    } catch(RuntimeException e) {
      Logger.err("编译失败!");
      Logger.err(e.getMessage());
      return;
    } catch(Exception e) {
      Logger.err("发生未知错误!");
      Logger.err(e);
      return;
    }
  }
}
