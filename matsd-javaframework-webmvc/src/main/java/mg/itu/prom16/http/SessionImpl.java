package mg.itu.prom16.http;

import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
        if (!has(key)) return defaultValue;

        return httpSession.getAttribute(key);
    }

    @Override
    public Map<String, Object> all() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        Enumeration<String> attributeNames = httpSession.getAttributeNames();

        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            sessionAttributes.put(key, httpSession.getAttribute(key));
        }

        return sessionAttributes;
    }

    @Override
    public boolean has(String key) {
        validateSessionKey(key);

        return httpSession.getAttribute(key) != null;
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

    @Override
    public FlashBag getFlashBag() {
        if (has(FlashBagImpl.STORAGE_KEY))
            return (FlashBag) get(FlashBagImpl.STORAGE_KEY);

        FlashBag flashBag = new FlashBagImpl();
        put(FlashBagImpl.STORAGE_KEY, flashBag);

        return flashBag;
    }

    @Override
    public void addFlash(String key, String message) {
        getFlashBag().add(key, message);
    }

    @Override
    public void addFlash(String key, String[] messages) {
        getFlashBag().add(key, messages);
    }

    private static void validateSessionKey(String key) {
        Assert.notBlank(key, false, "La clé de session ne peut pas être vide ou \"null\"");
    }
}
