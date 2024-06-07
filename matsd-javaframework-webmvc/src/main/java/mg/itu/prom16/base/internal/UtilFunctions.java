package mg.itu.prom16.base.internal;

import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.Get;
import mg.itu.prom16.annotations.Post;
import mg.itu.prom16.annotations.RequestMapping;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class UtilFunctions {
    private UtilFunctions() { }

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
