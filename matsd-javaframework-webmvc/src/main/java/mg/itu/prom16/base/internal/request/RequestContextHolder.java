package mg.itu.prom16.base.internal.request;

import mg.matsd.javaframework.core.utils.Assert;

public class RequestContextHolder {
    private static final ThreadLocal<ServletRequestAttributes> servletRequestAttributesHolder = new ThreadLocal<>();

    private RequestContextHolder() { }

    public static void setServletRequestAttributes(ServletRequestAttributes servletRequestAttributes) {
        servletRequestAttributesHolder.set(servletRequestAttributes);
    }

    public static ServletRequestAttributes getServletRequestAttributes() {
        ServletRequestAttributes servletRequestAttributes = servletRequestAttributesHolder.get();
        Assert.state(servletRequestAttributes != null,
            "Aucun servletRequestAttributes trouvé pour le thread courant");

        return servletRequestAttributes;
    }

    public static void clear() {
        servletRequestAttributesHolder.remove();
    }
}
