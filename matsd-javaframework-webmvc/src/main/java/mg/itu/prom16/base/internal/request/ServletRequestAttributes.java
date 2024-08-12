package mg.itu.prom16.base.internal.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.http.SessionImpl;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

public class ServletRequestAttributes {
    private final HttpServletRequest httpServletRequest;
    @Nullable
    private HttpServletResponse httpServletResponse;
    @Nullable
    private volatile Session session;

    public ServletRequestAttributes(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        this.httpServletRequest = httpServletRequest;
    }

    public ServletRequestAttributes(
        HttpServletRequest httpServletRequest,
        @Nullable HttpServletResponse httpServletResponse,
        Session session
    ) {
        this(httpServletRequest);
        this.httpServletResponse = httpServletResponse;
        this.session = session;
    }

    public HttpServletRequest getRequest() {
        return httpServletRequest;
    }

    @Nullable
    public HttpServletResponse getResponse() {
        return httpServletResponse;
    }

    @Nullable
    public Session getSession(boolean createIfNoCurrentSession) {
        if (session == null)
            synchronized (this) {
                if (session == null)
                    session = new SessionImpl().setHttpSession(httpServletRequest.getSession(createIfNoCurrentSession));
            }

        return session;
    }

    public Session getSession() {
        return getSession(true);
    }
}
