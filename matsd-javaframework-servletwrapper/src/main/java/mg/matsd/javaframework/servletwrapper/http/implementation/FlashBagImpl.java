package mg.matsd.javaframework.servletwrapper.http.implementation;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.servletwrapper.http.FlashBag;

import java.util.HashMap;
import java.util.Map;

public class FlashBagImpl implements FlashBag {
    public static final String STORAGE_KEY = "_matsd_flashes";

    private final Map<String, Object> flashes;

    public FlashBagImpl() {
        flashes = new HashMap<>();
    }

    @Override
    public void set(String key, @Nullable Object value) {
        validateKey(key);

        flashes.put(key, value);
    }

    @Override
    public void setAll(Map<String, ?> map) {
        Assert.notNull(map, "L'argument map ne peut pas être \"null\"");

        flashes.putAll(map);
    }

    @Override
    @Nullable
    public Object get(String key) {
        return get(key, null);
    }

    @Override
    @Nullable
    public Object get(String key, @Nullable Object defaultValue) {
        if (!has(key)) return defaultValue;

        Object result = flashes.get(key);
        flashes.remove(key);

        return result;
    }

    @Override
    @Nullable
    public Object peek(String key) {
        return peek(key, null);
    }

    @Override
    @Nullable
    public Object peek(String key, @Nullable Object defaultValue) {
        return has(key) ? flashes.get(key) : defaultValue;
    }

    @Override
    public Map<String, Object> peekAll() {
        return new HashMap<>(flashes);
    }

    @Override
    public Map<String, Object> all() {
        Map<String, Object> results = peekAll();
        flashes.clear();

        return results;
    }

    @Override
    public boolean has(String key) {
        validateKey(key);

        return flashes.containsKey(key);
    }

    private static void validateKey(String key) {
        Assert.notBlank(key, false, "La clé ne peut pas être vide ou \"null\"");
    }
}
