package mg.itu.prom16.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.base.FrontServlet;
import mg.itu.prom16.base.internal.request.RequestContext;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.support.ThirdPartyConfiguration;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.servletwrapper.http.FlashBag;
import mg.matsd.javaframework.servletwrapper.http.Request;
import mg.matsd.javaframework.servletwrapper.http.Session;

public abstract class WebFacade {
    static FrontServlet frontServlet;

    public static void setFrontServlet(final FrontServlet frontServlet) {
        Assert.notNull(frontServlet);

        WebFacade.frontServlet = frontServlet;
    }

    public static Request currentRequest() {
        return requestContext().getRequest();
    }

    public static HttpServletRequest currentHttpServletRequest() {
        return currentRequest().getRaw();
    }

    public static Session currentSession() {
        return requestContext().getSession();
    }

    public static HttpSession currentHttpSession() {
        return currentSession().getRaw();
    }

    public static FlashBag flashBag() {
        return currentSession().getFlashBag();
    }

    public static ObjectMapper objectMapper() {
        return (ObjectMapper) webApplicationContainer().getManagedInstance(ThirdPartyConfiguration.JACKSON_OBJECT_MAPPER_ID);
    }

    public static WebApplicationContainer webApplicationContainer() {
        return frontServlet.getWebApplicationContainer();
    }

    public static RequestContext requestContext() {
        return RequestContextHolder.getRequestContext();
    }
}
