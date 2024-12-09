package mg.itu.prom16.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.base.internal.request.ServletRequestAttributes;

public final class WebFacade {
    private WebFacade() { }

    public static HttpServletRequest getCurrentRequest() {
        return getServletRequestAttributes().getRequest();
    }

    public static HttpSession getCurrentSession() {
        return getServletRequestAttributes().getSession();
    }

    public static boolean isGetRequest() {
        return "GET".equals(getCurrentRequest().getMethod());
    }

    public static boolean isPostRequest() {
        return "POST".equals(getCurrentRequest().getMethod());
    }

    private static ServletRequestAttributes getServletRequestAttributes() {
        return RequestContextHolder.getServletRequestAttributes();
    }
}
