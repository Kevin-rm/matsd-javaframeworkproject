package mg.itu.prom16.utils;

import jakarta.servlet.http.HttpServletRequest;
import mg.matsd.javaframework.core.utils.Assert;

public final class WebUtils {
    private WebUtils() { }

    public static String baseUrl() {
        HttpServletRequest request = WebFacade.getCurrentRequest();

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

        if (!WebFacade.getCurrentRequest().getContextPath().endsWith("/") && !path.startsWith("/"))
            path = "/" + path;

        return baseUrl() + path;
    }
}
