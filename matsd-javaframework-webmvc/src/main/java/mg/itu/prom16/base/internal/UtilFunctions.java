package mg.itu.prom16.base.internal;

import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.exceptions.InvalidPackageException;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class UtilFunctions {
    private UtilFunctions() { }

    public static List<Class<?>> findControllers(String packageName) {
        Assert.notBlank(packageName, false, "Le nom de package ne peut pas être vide ou \"null\"");

        URL url = Thread.currentThread()
            .getContextClassLoader()
            .getResource(packageName.replace('.', '/'));

        if (url == null) throw new InvalidPackageException(packageName);

        List<Class<?>> controllers = new ArrayList<>();

        File[] files = new File(url.getPath()).listFiles();
        if (files == null) return controllers;

        for (File file : files) {
            String fileName = file.getName();

            if (file.isDirectory())
                controllers.addAll(
                    findControllers(String.format("%s.%s", packageName, fileName))
                );

            try {
                Class<?> clazz = Class.forName(
                    String.format("%s.%s", packageName, fileName.replaceAll("\\.class$", ""))
                );

                if (isController(clazz)) controllers.add(clazz);
            } catch (ClassNotFoundException ignored) { }
        }

        return controllers;
    }

    public static boolean isController(@Nullable Class<?> clazz) {
        if (clazz == null) return false;

        return clazz.isAnnotationPresent(Controller.class);
    }
}
