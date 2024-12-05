package mg.itu.prom16.http;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.*;

public class FlashBagImpl implements FlashBag {
    public static final String STORAGE_KEY = "_matsd_flashes";

    private final Map<String, List<Object>> flashes;

    public FlashBagImpl() {
        flashes = new HashMap<>();
    }

    @Override
    public void add(String key, Object value) {
        validateKey(key);
        Assert.notNull(value, "La valeur ne peut pas être \"null\"");

        List<Object> flashValues = flashes.computeIfAbsent(key, k -> new ArrayList<>());
        flashValues.add(value);
    }

    @Override
    public void add(String key, Object[] values) {
        validateKey(key);
        Assert.notNull(values, "La liste des valeurs ne peut pas être \"null\"");
        Assert.noNullElements(values, "Chaque valeur dans la liste ne peut pas être \"null\"");

        List<Object> flashValues = flashes.computeIfAbsent(key, k -> new ArrayList<>());
        flashValues.addAll(List.of(values));
    }

    @Override
    public void set(String key, Object[] values) {
        validateKey(key);
        Assert.notNull(values, "La liste de valeurs ne peut pas être \"null\"");
        Assert.noNullElements(values, "Chaque valeur dans la liste ne peut pas être \"null\"");

        flashes.put(key, List.of(values));
    }

    @Override
    @Nullable
    public Object[] get(String key) {
        return get(key, null);
    }

    @Override
    @Nullable
    public Object[] get(String key, @Nullable Object[] defaultValue) {
        if (!has(key)) return defaultValue;

        List<Object> results = flashes.get(key);
        flashes.remove(key);

        return results.toArray(new Object[0]);
    }

    @Override
    @Nullable
    public Object[] peek(String key) {
        return peek(key, null);
    }

    @Override
    @Nullable
    public Object[] peek(String key, @Nullable Object[] defaultValue) {
        return has(key) ? flashes.get(key).toArray(new Object[0]) : defaultValue;
    }

    @Override
    public Map<String, Object[]> peekAll() {
        Map<String, Object[]> results = new HashMap<>();
        flashes.forEach((key, value) -> results.put(key, value.toArray(new Object[0])));

        return results;
    }

    @Override
    public Map<String, Object[]> all() {
        Map<String, Object[]> results = peekAll();
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
