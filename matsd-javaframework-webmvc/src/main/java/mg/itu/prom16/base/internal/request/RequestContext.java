package mg.itu.prom16.base.internal.request;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.http.base.Request;
import mg.matsd.javaframework.http.base.Response;
import mg.matsd.javaframework.http.base.Session;

public class RequestContext {
    private final Request  request;
    @Nullable
    private final Response response;
    @Nullable
    private volatile Session session;

    public RequestContext(Request request, @Nullable Response response) {
        Assert.notNull(request, "La requête ne peut pas être \"null\"");

        this.request  = request;
        this.response = response;
    }

    public RequestContext(Request request) {
        this(request, null);
    }

    public Request getRequest() {
        return request;
    }

    @Nullable
    public Response getResponse() {
        return response;
    }

    @Nullable
    public Session getSession(boolean createIfNoSession) {
        if (session == null)
            synchronized (this) {
                if (session == null)
                    session = request.getSession(createIfNoSession);
            }

        return session;
    }

    public Session getSession() {
        return getSession(true);
    }
}
