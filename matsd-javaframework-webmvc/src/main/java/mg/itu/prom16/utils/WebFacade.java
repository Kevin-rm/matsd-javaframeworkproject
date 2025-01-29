package mg.itu.prom16.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.base.FrontServlet;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.base.internal.request.ServletRequestAttributes;
import mg.itu.prom16.http.FlashBag;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.http.SessionImpl;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.utils.Assert;

public final class WebFacade {
    static FrontServlet frontServlet;

    private WebFacade() { }

    public static void setFrontServlet(final FrontServlet frontServlet) {
        Assert.notNull(frontServlet);

        WebFacade.frontServlet = frontServlet;
    }

    public static HttpServletRequest getCurrentRequest() {
        return getServletRequestAttributes().getRequest();
    }

    public static HttpSession getCurrentHttpSession() {
        return getServletRequestAttributes().getSession();
    }

    public static Session getCurrentSession() {
        return (Session) getCurrentHttpSession()
            .getAttribute(WebApplicationContainer.WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + SessionImpl.MANAGED_INSTANCE_ID);
    }

    public static FlashBag getFlashBag() {
        return getCurrentSession().getFlashBag();
    }

    public static boolean isGetRequest() {
        return "GET".equals(getCurrentRequest().getMethod());
    }

    public static boolean isPostRequest() {
        return "POST".equals(getCurrentRequest().getMethod());
    }

    public static WebApplicationContainer getWebApplicationContainer() {
        return frontServlet.getWebApplicationContainer();
    }

    private static ServletRequestAttributes getServletRequestAttributes() {
        return RequestContextHolder.getServletRequestAttributes();
    }
}
