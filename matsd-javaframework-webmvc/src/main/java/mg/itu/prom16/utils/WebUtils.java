package mg.itu.prom16.utils;

import jakarta.servlet.http.HttpServletRequest;
import mg.matsd.javaframework.core.annotations.Nullable;

public final class WebUtils {

    private WebUtils() { }

    public static boolean isGet(@Nullable HttpServletRequest request) {
        return request != null && "GET".equals(request.getMethod());
    }

    public static boolean isPost(@Nullable HttpServletRequest request) {
        return request != null && "POST".equals(request.getMethod());
    }
}
