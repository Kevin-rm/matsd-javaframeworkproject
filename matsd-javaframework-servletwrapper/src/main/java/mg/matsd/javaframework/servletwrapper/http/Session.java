package mg.matsd.javaframework.servletwrapper.http;

import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.servletwrapper.base.internal.UtilFunctions;

import java.util.HashMap;
import java.util.Map;

public class Session {
    protected final HttpSession raw;
    @Nullable
    private Map<String, Object> attributes;

    Session(HttpSession raw) {
        this.raw = raw;
    }

    public HttpSession getRaw() {
        return raw;
    }

    @Nullable
    public Object get(String key) {
        validateSessionKey(key);

        return raw.getAttribute(key);
    }

    @Nullable
    public Object getOrCreate(String key, @Nullable Object defaultValue) {
        final Object attribute = get(key);
        if (attribute == null) {
            set(key, defaultValue);
            return defaultValue;
        }

        return attribute;
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> all() {
        if (attributes != null) return attributes;

        attributes = (Map<String, Object>) UtilFunctions.collectKeyValues(raw.getAttributeNames(), raw::getAttribute);
        return attributes;
    }

    public Session set(String key, @Nullable Object value) {
        validateSessionKey(key);

        raw.setAttribute(key, value);

        if (attributes == null) attributes = new HashMap<>();
        if (value == null) attributes.remove(key);
        else attributes.put(key, value);

        return this;
    }

    public Session remove(String key) {
        validateSessionKey(key);

        raw.removeAttribute(key);
        if (attributes != null) attributes.remove(key);

        return this;
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
