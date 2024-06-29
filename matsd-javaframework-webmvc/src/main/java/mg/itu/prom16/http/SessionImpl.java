package mg.itu.prom16.http;

import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

public class SessionImpl implements Session {
    private final HttpSession httpSession;

    public SessionImpl(final HttpSession httpSession) {
        Assert.notNull(httpSession, "L'argument httpSession ne peut pas être \"null\"");

        this.httpSession = httpSession;
    }

    @Override
    public Object get(String key) {
        return get(key, null);
    }

    @Override
    public Object get(String key, @Nullable Object defaultValue) {
        validateSessionKey(key);

        Object sessionAttribute = httpSession.getAttribute(key);
        return sessionAttribute == null ? defaultValue : sessionAttribute;
    }

    @Override
    public boolean has(String key) {
        validateSessionKey(key);

        return get(key) != null;
    }

    @Override
    public void put(String key, @Nullable Object value) {
        validateSessionKey(key);

        httpSession.setAttribute(key, value);
    }

    @Override
    public void remove(String key) {
        validateSessionKey(key);

        httpSession.removeAttribute(key);
    }

    @Override
    public void invalidate() {
        httpSession.invalidate();
    }

    private static void validateSessionKey(String key) {
        Assert.notBlank("La clé de session ne peut pas être vide ou \"null\"", false);
    }
}
