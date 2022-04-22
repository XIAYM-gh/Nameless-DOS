package org.glamey.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 自定义动态编译加载器
 * <p>
 * @author zhouyang01
 * Created on 20220412.
 */
public class DynamicClassLoader extends ClassLoader {

    private final Map<String, JavaClassFileObject> classFileObjectMap = new HashMap<>();

    public DynamicClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    /**
     * 不要破坏双亲委派模型，默认先让父类加载
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        JavaClassFileObject classFileObject = classFileObjectMap.get(name);
        if (classFileObject == null) {
            return super.findClass(name);
        }
        return super.defineClass(name, classFileObject.getBytes(), 0, classFileObject.getBytes().length);
    }

    /**
     * 保存内部类
     */
    public void registerCompiledSource(JavaClassFileObject javaClassFileObject) {
        classFileObjectMap.put(javaClassFileObject.getClassName(), javaClassFileObject);
    }

    /**
     * 获取动态类字节码
     */
    public Map<String, byte[]> getBytes() {
        Map<String, byte[]> byteMap = new HashMap<>(classFileObjectMap.size());
        for (Entry<String, JavaClassFileObject> entry : classFileObjectMap.entrySet()) {
            byteMap.put(entry.getKey(), entry.getValue().getBytes());
        }
        return byteMap;
    }

    /**
     * 获取动态类实例
     */
    public Map<String, Class<?>> getClasses() throws ClassNotFoundException {
        Map<String, Class<?>> classMap = new HashMap<>(classFileObjectMap.size());
        for (Entry<String, JavaClassFileObject> entry : classFileObjectMap.entrySet()) {
            classMap.put(entry.getValue().getClassName(), findClass(entry.getValue().getClassName()));
        }
        return classMap;
    }
}
