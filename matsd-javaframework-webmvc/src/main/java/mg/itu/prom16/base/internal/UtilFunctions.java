package mg.itu.prom16.base.internal;

import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.Get;
import mg.itu.prom16.annotations.Post;
import mg.itu.prom16.annotations.RequestMapping;
import mg.itu.prom16.exceptions.InvalidPackageException;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UtilFunctions {
    private UtilFunctions() { }

    public static List<Class<?>> findControllers(@Nullable String packageName) {
        if (packageName == null) packageName = "";

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

    public static Map<String, Object> getRequestMappingInfoAttributes(Method method) {
        String path = "";

        RequestMapping requestMapping = (RequestMapping) AnnotationUtils.getAnnotation(RequestMapping.class, method);
        if (method.isAnnotationPresent(RequestMapping.class))
            path = requestMapping.value();
        else if (method.isAnnotationPresent(Get.class))
            path = method.getAnnotation(Get.class).value();
        else if (method.isAnnotationPresent(Post.class))
            path = method.getAnnotation(Post.class).value();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", path);
        attributes.put("methods", requestMapping.methods());

        return attributes;
    }
}
