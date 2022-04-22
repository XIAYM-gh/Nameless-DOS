package org.glamey.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

/**
 * 动态类文件管理器
 * <p>
 *
 * @author zhouyang01
 * Created on 20220412.
 */
public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final DynamicClassLoader classLoader;
    private final List<JavaClassFileObject> javaClassFileObjects = new ArrayList<>();
    private static final String[] superLocationNames = {StandardLocation.PLATFORM_CLASS_PATH.name(),
            /** JPMS StandardLocation.SYSTEM_MODULES **/
            "SYSTEM_MODULES"};
    private final PackageInternalsFinder finder;

    protected DynamicJavaFileManager(JavaFileManager fileManager, DynamicClassLoader classLoader) {
        super(fileManager);
        this.classLoader = classLoader;
        this.finder = new PackageInternalsFinder(classLoader);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return this.classLoader;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
        for (JavaClassFileObject classFileObject : javaClassFileObjects) {
            if (classFileObject.getClassName().equals(className)) {
                return classFileObject;
            }
        }
        //处理内部类
        JavaClassFileObject innerClassFileObject = new JavaClassFileObject(className);
        javaClassFileObjects.add(innerClassFileObject);
        classLoader.registerCompiledSource(innerClassFileObject);
        return innerClassFileObject;
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof CustomJavaFileObject) {
            return ((CustomJavaFileObject) file).binaryName();
        } else {
            /**
             * if it's not CustomJavaFileObject, then it's coming from standard file manager
             * - let it handle the file
             */
            return super.inferBinaryName(location, file);
        }
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds,
            boolean recurse) throws IOException {
        if (location instanceof StandardLocation) {
            String locationName = ((StandardLocation) location).name();
            for (String name : superLocationNames) {
                if (name.equals(locationName)) {
                    return super.list(location, packageName, kinds, recurse);
                }
            }
        }

        // merge JavaFileObjects from specified ClassLoader
        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            return new IterableJoin<JavaFileObject>(super.list(location, packageName, kinds, recurse),
                    finder.find(packageName));
        }

        return super.list(location, packageName, kinds, recurse);
    }

    static class IterableJoin<T> implements Iterable<T> {
        private final Iterable<T> first, next;

        public IterableJoin(Iterable<T> first, Iterable<T> next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public Iterator<T> iterator() {
            return new IteratorJoin<T>(first.iterator(), next.iterator());
        }
    }

    static class IteratorJoin<T> implements Iterator<T> {
        private final Iterator<T> first, next;

        public IteratorJoin(Iterator<T> first, Iterator<T> next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public boolean hasNext() {
            return first.hasNext() || next.hasNext();
        }

        @Override
        public T next() {
            if (first.hasNext()) {
                return first.next();
            }
            return next.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
