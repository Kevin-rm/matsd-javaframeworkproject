package mg.itu.prom16.utils;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.servletwrapper.http.Request;

public abstract class WebUtils {
    
    public static String baseUrl() {
        Request request = WebFacade.currentRequest();

        StringBuilder stringBuilder = new StringBuilder(request.getProtocol())
            .append("://")
            .append(request.getHost());

        int port = request.getPort();
        if (port != 80 && port != 443)
            stringBuilder.append(":").append(port);

        return stringBuilder
            .append(request.getContextPath())
            .toString();
    }

    public static String absolutePath(String path) {
        Assert.notBlank(path, false, "L'argument path ne peut pas être vide ou \"null\"");

        if (!WebFacade.currentRequest().getContextPath().endsWith("/") && !path.startsWith("/"))
            path = "/" + path;

        return baseUrl() + path;
    }
}
