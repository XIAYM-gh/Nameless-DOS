package org.glamey.compiler;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * java源文件包装类
 * <p>
 *
 * @author zhouyang01
 * Created on 20220412.
 */
public class JavaSourceFileObject extends SimpleJavaFileObject {

    private final String javaSourceContent;

    public JavaSourceFileObject(String sourceName, String javaSourceContent) {
        super(URI.create("string:///" + sourceName.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.javaSourceContent = javaSourceContent;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return javaSourceContent;
    }
}
