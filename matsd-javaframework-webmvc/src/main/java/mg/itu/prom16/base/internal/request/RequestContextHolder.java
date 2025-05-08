package mg.itu.prom16.base.internal.request;

import mg.matsd.javaframework.core.utils.Assert;

public class RequestContextHolder {
    private static final ThreadLocal<RequestContext> THREAD_LOCAL = new ThreadLocal<>();

    private RequestContextHolder() { }

    public static void setRequestContext(RequestContext requestContext) {
        Assert.notNull(requestContext);
        THREAD_LOCAL.set(requestContext);
    }

    public static RequestContext getRequestContext() {
        RequestContext requestContext = THREAD_LOCAL.get();
        Assert.state(requestContext != null,
            "Aucun requestAttributes trouvé pour le thread courant");

        return requestContext;
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
