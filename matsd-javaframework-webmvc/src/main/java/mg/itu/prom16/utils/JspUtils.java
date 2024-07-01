package mg.itu.prom16.utils;

import jakarta.servlet.http.HttpServletRequest;
import mg.matsd.javaframework.core.utils.Assert;

public final class JspUtils {
    private JspUtils() { }

    public static String baseUrl(HttpServletRequest request) {
        Assert.notNull(request, "L'argument request ne peut pas être \"null\"");

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

    public static String absolutePath(HttpServletRequest request, String path) {
        Assert.notNull(request, "L'argument request ne peut pas être \"null\"");
        Assert.notBlank(path, false, "L'argument path ne peut pas être vide ou \"null\"");

        if (!request.getContextPath().endsWith("/") && !path.startsWith("/"))
            path = "/" + path;

        return baseUrl(request) + path;
    }
}
