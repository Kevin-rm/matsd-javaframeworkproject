package mg.matsd.javaframework.servletwrapper.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

public class Request {
    protected final HttpServletRequest raw;
    @Nullable
    protected Session session;

    public Request(HttpServletRequest raw) {
        Assert.notNull(raw, "L'argument raw ne peut pas être \"null\"");
        this.raw = raw;
    }

    public HttpServletRequest getRaw() {
        return raw;
    }

    public Session getSession() {
        HttpSession httpSession = raw.getSession();
        if (httpSession == null) httpSession = raw.getSession(true);

        return session == null ? session = new Session(httpSession) : session;
    }
}
