package mg.itu.prom16.base.internal.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

public class ServletRequestAttributes {
    private final HttpServletRequest httpServletRequest;
    @Nullable
    private HttpServletResponse httpServletResponse;
    @Nullable
    private volatile HttpSession httpSession;

    public ServletRequestAttributes(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        this.httpServletRequest = httpServletRequest;
    }

    public ServletRequestAttributes(HttpServletRequest httpServletRequest, @Nullable HttpServletResponse httpServletResponse) {
        this(httpServletRequest);
        this.httpServletResponse = httpServletResponse;
    }

    public HttpServletRequest getRequest() {
        return httpServletRequest;
    }

    public HttpServletResponse getResponse() {
        return httpServletResponse;
    }

    @Nullable
    public HttpSession getSession(boolean createIfNoCurrentSession) {
        if (httpSession == null)
            synchronized (this) {
                if (httpSession == null)
                    httpSession = httpServletRequest.getSession(createIfNoCurrentSession);
            }

        return httpSession;
    }

    public HttpSession getSession() {
        return getSession(true);
    }
}
