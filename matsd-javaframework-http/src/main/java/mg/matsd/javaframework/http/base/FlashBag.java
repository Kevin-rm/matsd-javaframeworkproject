package mg.matsd.javaframework.http.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.HashMap;
import java.util.Map;

public class FlashBag {
    public static final String STORAGE_KEY = "_matsd_flashes";

    private final Map<String, Object> flashes;

    FlashBag() {
        flashes = new HashMap<>();
    }

    @Nullable
    public Object get(String key) {
        return get(key, null);
    }

    @Nullable
    public Object get(String key, @Nullable Object defaultValue) {
        if (!has(key)) return defaultValue;

        Object result = flashes.get(key);
        flashes.remove(key);

        return result;
    }

    public boolean has(String key) {
        validateKey(key);

        return flashes.containsKey(key);
    }

    public Map<String, Object> all() {
        Map<String, Object> results = peekAll();
        flashes.clear();

        return results;
    }

    @Nullable
    public Object peek(String key) {
        return peek(key, null);
    }

    @Nullable
    public Object peek(String key, @Nullable Object defaultValue) {
        return has(key) ? flashes.get(key) : defaultValue;
    }

    public Map<String, Object> peekAll() {
        return new HashMap<>(flashes);
    }

    public void set(String key, @Nullable Object value) {
        validateKey(key);

        flashes.put(key, value);
    }

    public void setAll(Map<String, ?> map) {
        Assert.notNull(map, "L'argument map ne peut pas être \"null\"");

        flashes.putAll(map);
    }

    private static void validateKey(String key) {
        Assert.notBlank(key, false, "La clé ne peut pas être vide ou \"null\"");
    }
}
