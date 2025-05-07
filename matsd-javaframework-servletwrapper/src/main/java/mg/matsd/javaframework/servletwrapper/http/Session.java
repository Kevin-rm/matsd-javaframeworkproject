package mg.matsd.javaframework.servletwrapper.http;

import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.servletwrapper.base.internal.UtilFunctions;

import java.util.Map;

public class Session {
    protected final HttpSession raw;
    @Nullable
    private Map<String, Object> attributes;

    Session(HttpSession raw) {
        this.raw = raw;
    }

    @Nullable
    public Object get(String key) {
        return get(key, null);
    }

    @Nullable
    public Object get(String key, @Nullable Object defaultValue) {
        validateSessionKey(key);

        Object attribute = raw.getAttribute(key);
        return attribute == null ? defaultValue : attribute;
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    public Map<String, Object> all() {
        if (attributes != null) return attributes;

        attributes = UtilFunctions.collectAttributes(raw.getAttributeNames(), raw::getAttribute);
        return attributes;
    }

    public void set(String key, @Nullable Object value) {
        validateSessionKey(key);

        raw.setAttribute(key, value);
    }

    public void remove(String key) {
        validateSessionKey(key);

        raw.removeAttribute(key);
    }

    public void clear() {
        raw.invalidate();
    }

    public FlashBag getFlashBag() {
        if (has(FlashBag.STORAGE_KEY))
            return (FlashBag) get(FlashBag.STORAGE_KEY);

        FlashBag flashBag = new FlashBag();
        set(FlashBag.STORAGE_KEY, flashBag);

        return flashBag;
    }

    public void addFlash(String key, Object value) {
        getFlashBag().set(key, value);
    }

    private static void validateSessionKey(String key) {
        Assert.notBlank(key, false, "La clé de session ne peut pas être vide ou \"null\"");
    }
}
