package org.glamey.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import static cn.xiaym.utils.LanguageUtil.Lang;

/**
 * 动态编译Java源文件（编译入口）
 * <p><p>
 * 具体实现详见 {@link JavaCompiler} 上面的注释.
 * <p>
 * {@link StandardJavaFileManager}标准的文件管理器，自定义实现${@link DynamicJavaFileManager}
 * <p>
 * {@link DiagnosticCollector} 诊断收集器，通过{@link Kind}来判断编译的问题类型
 * <p>
 *
 * @author zhouyang01
 * Created on 20220412.
 */
public class DynamicJavaCompiler {
    private final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    private final List<JavaFileObject> compilationUnits = new ArrayList<>();
    private final StandardJavaFileManager standardFileManager;
    private final DynamicClassLoader classLoader;
    private final List<Diagnostic<? extends JavaFileObject>> compilerWarnings = new ArrayList<>();
    private final List<Diagnostic<? extends JavaFileObject>> compilerErrors = new ArrayList<>();
    private final List<String> options = new ArrayList<>();


    public DynamicJavaCompiler(ClassLoader classLoader) {
        //compiler option 暂时不使用
        this.standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
        this.classLoader = new DynamicClassLoader(classLoader);
        options.add("-Xlint:unchecked");
    }

    public void addSource(String javaSourceName, String javaSourceContent) {
        add(new JavaSourceFileObject(javaSourceName, javaSourceContent));
    }

    private void add(JavaFileObject javaFileObject) {
        compilationUnits.add(javaFileObject);
    }

    public Map<String, byte[]> genClassBytes() {
        //清空编译异常
        compilerWarnings.clear();
        compilerErrors.clear();

        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        DynamicJavaFileManager fileManager =
                new DynamicJavaFileManager(standardFileManager, classLoader);
        CompilationTask compilationTask =
                javaCompiler.getTask(null, fileManager, diagnosticCollector, options, null, compilationUnits);

        try {
            if (compilationUnits.isEmpty()) {
                return classLoader.getBytes();
            }
            compile(diagnosticCollector, compilationTask);
            return classLoader.getBytes();
        } catch (Exception error) {
            throw new RuntimeException(getErrorMessage(compilerErrors));
        } finally {
            compilationUnits.clear();
            try {
                fileManager.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close the file manager.", e);
            }
        }
    }


    public Map<String, Class<?>> genClasses() {
        //清空编译异常
        compilerWarnings.clear();
        compilerErrors.clear();

        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        DynamicJavaFileManager fileManager =
                new DynamicJavaFileManager(standardFileManager, classLoader);
        CompilationTask compilationTask =
                javaCompiler.getTask(null, fileManager, diagnosticCollector, null, null, compilationUnits);

        try {
            if (compilationUnits.isEmpty()) {
                return classLoader.getClasses();
            }

            compile(diagnosticCollector, compilationTask);
            return classLoader.getClasses();
        } catch (Throwable error) {
            throw new RuntimeException(getErrorMessage(compilerErrors));
        } finally {
            compilationUnits.clear();
            try {
                fileManager.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close the file manager.", e);
            }
        }
    }

    private void compile(DiagnosticCollector<JavaFileObject> diagnosticCollector, CompilationTask compilationTask) {
        boolean call = compilationTask.call();
        //编译失败
        List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticCollector.getDiagnostics();
        if (!call && diagnostics.size() > 0) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                switch (diagnostic.getKind()) {
                    case NOTE:
                    case WARNING:
                    case MANDATORY_WARNING:
                        compilerWarnings.add(diagnostic);
                        break;
                    case ERROR:
                    case OTHER:
                    default:
                        compilerErrors.add(diagnostic);
                        break;
                }
            }
            if (compilerErrors.size() > 0) {
                throw new RuntimeException(getErrorMessage(compilerErrors));
            }
        }
    }

    private String getErrorMessage(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        return diagnostics.stream()
                .map(diagnostic ->
                        Lang("sjava.line_num") + ": " + diagnostic.getLineNumber() + " " + Lang("sjava.message") + ": " + diagnostic.getMessage(Locale.getDefault()))
                .collect(Collectors.joining("\r\n"));
    }
}
