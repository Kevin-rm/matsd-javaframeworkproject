package mg.itu.prom16.utils;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.matsd.javaframework.core.utils.Assert;

public final class WebUtils {

    private WebUtils() { }

    public static HttpServletRequest getCurrentRequest() {
        return RequestContextHolder.getServletRequestAttributes().getRequest();
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
}
