package org.glamey.compiler;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;


/**
 * Java class 包装类
 * <p>
 *
 * @author zhouyang01
 * Created on 20220412.
 */
public class JavaClassFileObject extends SimpleJavaFileObject {

    private ByteArrayOutputStream byteArrayOutputStream;

    protected JavaClassFileObject(String javaClassName) {
        super(URI.create("byte:///" + javaClassName.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
        if (byteArrayOutputStream == null) {
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
        return byteArrayOutputStream;
    }

    public byte[] getBytes() {
        return byteArrayOutputStream.toByteArray();
    }

    public String getClassName() {
        String className = getName();
        className = className.replace('/', '.');
        className = className.substring(1, className.indexOf(Kind.CLASS.extension));
        return className;
    }
}
