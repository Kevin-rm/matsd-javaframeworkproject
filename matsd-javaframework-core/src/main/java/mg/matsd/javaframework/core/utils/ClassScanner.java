package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.exceptions.PackageNotFoundException;

import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

public abstract class ClassScanner {

    public static void doScan(String packageName, Consumer<Class<?>> action) throws PackageNotFoundException {
        Assert.notBlank(packageName, false);
        Assert.notNull(action);

        URL url = Thread.currentThread()
            .getContextClassLoader()
            .getResource(packageName.replace('.', '/'));

        if (url == null) throw new PackageNotFoundException(packageName);

        File[] files = new File(url.getPath()).listFiles();
        if (files == null) return;

        for (File file : files) {
            String fileName = file.getName();

            if (file.isDirectory()) doScan(String.format("%s.%s", packageName, fileName), action);
            else if (fileName.endsWith(".class")) {
                try {
                    action.accept(Class.forName(
                        String.format("%s.%s", packageName, fileName.replaceAll("\\.class$", ""))
                    ));
                } catch (ClassNotFoundException ignored) { }
            }
        }
    }
}
