package mg.itu.prom16.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.FrontServlet;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.base.internal.request.ServletRequestAttributes;
import mg.itu.prom16.http.FlashBag;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.http.SessionImpl;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

public final class WebUtils {
    private static FrontServlet frontServlet;

    private WebUtils() { }

    public static void setFrontServlet(final FrontServlet frontServlet) {
        Assert.notNull(frontServlet);

        WebUtils.frontServlet = frontServlet;
    }

    public static boolean isGetRequest() {
        return "GET".equals(getCurrentRequest().getMethod());
    }

    public static boolean isPostRequest() {
        return "POST".equals(getCurrentRequest().getMethod());
    }

    public static String baseUrl() {
        HttpServletRequest request = getCurrentRequest();

        StringBuilder stringBuilder = new StringBuilder(request.getScheme())
            .append("://")
            .append(request.getServerName());

        int port = request.getServerPort();
        if (port != 80 && port != 443)
            stringBuilder.append(":").append(port);

        return stringBuilder
            .append(request.getContextPath())
            .toString();
    }

    public static String absolutePath(String path) {
        Assert.notBlank(path, false, "L'argument path ne peut pas être vide ou \"null\"");

        if (!getCurrentRequest().getContextPath().endsWith("/") && !path.startsWith("/"))
            path = "/" + path;

        return baseUrl() + path;
    }

    @Nullable
    public static String[] flashMessages(String key) {
        return getFlashBag().get(key);
    }

    @Nullable
    public static String[] flashMessages(String key, @Nullable String[] defaultValue) {
        return getFlashBag().get(key, defaultValue);
    }

    public static String routeTo(String name) throws ServletException {
        Assert.notBlank(name, false, "Le nom de la route ne peut pas être vide ou \"null\"");

        return absolutePath(frontServlet.getRequestMappingInfoByName(name.strip()).getPath());
    }

    private static FlashBag getFlashBag() {
        return ((Session) getServletRequestAttributes().getSession()
            .getAttribute(WebApplicationContainer.WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + SessionImpl.MANAGED_INSTANCE_ID)
        ).getFlashBag();
    }

    private static HttpServletRequest getCurrentRequest() {
        return getServletRequestAttributes().getRequest();
    }

    private static ServletRequestAttributes getServletRequestAttributes() {
        return RequestContextHolder.getServletRequestAttributes();
    }
}
