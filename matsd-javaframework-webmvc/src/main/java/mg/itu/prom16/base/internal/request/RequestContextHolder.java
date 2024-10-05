package mg.itu.prom16.base.internal.request;

import mg.matsd.javaframework.core.utils.Assert;

public class RequestContextHolder {
    private static final ThreadLocal<ServletRequestAttributes> SERVLET_REQUEST_ATTRIBUTES_HOLDER = new ThreadLocal<>();

    private RequestContextHolder() { }

    public static void setServletRequestAttributes(ServletRequestAttributes servletRequestAttributes) {
        SERVLET_REQUEST_ATTRIBUTES_HOLDER.set(servletRequestAttributes);
    }

    public static ServletRequestAttributes getServletRequestAttributes() {
        ServletRequestAttributes servletRequestAttributes = SERVLET_REQUEST_ATTRIBUTES_HOLDER.get();
        Assert.state(servletRequestAttributes != null,
            "Aucun servletRequestAttributes trouvé pour le thread courant");

        return servletRequestAttributes;
    }

    public static void clear() {
        SERVLET_REQUEST_ATTRIBUTES_HOLDER.remove();
    }
}
