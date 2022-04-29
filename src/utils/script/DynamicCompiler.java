package cn.xiaym.utils;

import static cn.xiaym.utils.LanguageUtil.Lang;

import org.glamey.compiler.*;

import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import javax.tools.*;

public class DynamicCompiler {
  public static void compile(String fileName) {
    if(ToolProvider.getSystemJavaCompiler() == null) {
       Logger.err(Lang("sjava.no_compiler"));
       return;
    }

    String ClassName = fileName.trim().substring(0, fileName.trim().length() - 5); //Remove .java
    Path f = Paths.get(fileName);
    
    if(!Files.exists(f)) {
      Logger.err(Lang("sjava.not_exists"));
      return;
    }

    DynamicJavaCompiler comp = new DynamicJavaCompiler(new NullClass().getClass().getClassLoader());

    List<String> RawContentList;
    LinkedList<String> contentList = new LinkedList<>();

    try {
      RawContentList = Files.readAllLines(f);
    } catch(IOException e) {
      Logger.err(Lang("sjava.io_err"));
      return;
    }

    // 默认导入以下内容
    contentList.add("import cn.xiaym.utils.*;");
    contentList.add("import cn.xiaym.ndos.*;");
    contentList.add("import cn.xiaym.ndos.console.*;");
    contentList.add("import cn.xiaym.ndos.plugins.*;");
    contentList.add("import cn.xiaym.ndos.command.*;");
    contentList.add("import java.io.*;");
    contentList.add("import java.nio.*;");
    contentList.add("import java.nio.charset.*;");
    contentList.add("import java.nio.file.*;");
    contentList.add("import java.util.*;");
    contentList.add("import java.time.*;");

    for(String s : RawContentList) {
      if(!s.trim().startsWith("package")) contentList.add(s);
    }

    String sourceContent = String.join("", contentList);
    comp.addSource(ClassName, sourceContent);

    try {
      Map<String, Class<?>> classMap = comp.genClasses();

      Class<?> compiledClass = (Class<?>) classMap.values().toArray()[0];;

      if(compiledClass == null) {
        Logger.err(Lang("sjava.bytecode_null"));
        return;
      }

      Method m = compiledClass.getDeclaredMethod("main");

      if (m == null) {
        Logger.err(Lang("sjava.no_main"));
        return;
      }

      m.setAccessible(true);
      m.invoke(compiledClass.getDeclaredConstructor().newInstance());
    } catch(RuntimeException e) {
      Logger.err(Lang("sjava.failed_2compile"));
      Logger.err(e.getMessage());
      return;
    } catch(Exception e) {
      Logger.err(Lang("sjava.failed"));
      Logger.err(e);
      return;
    }
  }
}
