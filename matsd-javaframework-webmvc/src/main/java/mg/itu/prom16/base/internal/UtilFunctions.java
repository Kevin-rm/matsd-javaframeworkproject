package mg.itu.prom16.base.internal;

import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.Get;
import mg.itu.prom16.annotations.Post;
import mg.itu.prom16.annotations.RequestMapping;
import mg.itu.prom16.base.RequestMethod;
import mg.itu.prom16.exceptions.InvalidPackageException;
import mg.matsd.javaframework.core.annotations.Nullable;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public static boolean isAnnotatedWithRequestMappingAndItsShortcuts(
        @Nullable Method method
    ) {
        return method != null &&
               (
                   method.isAnnotationPresent(RequestMapping.class) ||
                   method.isAnnotationPresent(Get.class)
               );
    }

    @Nullable
    public static RequestMapping requestMapping(Method method) {
        if (method == null) return null;

        boolean breakLoop = false;

        RequestMapping requestMapping = null;
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof RequestMapping) {
                requestMapping = (RequestMapping) annotation;

                breakLoop = true;
            } else if (UtilFunctions.instanceofRequestMappingShortcuts(annotation)) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                requestMapping = new RequestMapping() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return RequestMapping.class;
                    }

                    @Override
                    public String path() {
                        try {
                            return (String) annotationType.getDeclaredMethod("path").invoke(annotation);
                        } catch (Exception ignored) {
                            return "";
                        }
                    }

                    @Override
                    public RequestMethod[] methods() {
                        return annotationType
                            .getAnnotation(RequestMapping.class)
                            .methods();
                    }
                };

                breakLoop = true;
            }

            if (breakLoop) break;
        }

        return requestMapping;
    }

    private static boolean instanceofRequestMappingShortcuts(Annotation annotation) {
        return annotation instanceof Get || annotation instanceof Post;
    }
}
