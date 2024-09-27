package mg.itu.prom16.http;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.*;

public class FlashBagImpl implements FlashBag {
    public static final String STORAGE_KEY = "_matsd_flashes";

    private final Map<String, List<String>> flashes;

    public FlashBagImpl() {
        flashes = new HashMap<>();
    }

    @Override
    public void add(String key, String message) {
        validateKey(key);
        Assert.notNull(message, "Le message ne peut pas être \"null\"");

        List<String> flashMessages = flashes.computeIfAbsent(key, k -> new ArrayList<>());
        flashMessages.add(message);
    }

    @Override
    public void add(String key, String[] messages) {
        validateKey(key);
        Assert.notNull(messages, "La liste de messages ne peut pas être \"null\"");
        Arrays.stream(messages).forEachOrdered(message -> Assert.notNull(message, "Chaque message de la liste ne peut pas être \"null\""));

        List<String> flashMessages = flashes.computeIfAbsent(key, k -> new ArrayList<>());
        flashMessages.addAll(List.of(messages));
    }

    @Override
    public void set(String key, String[] messages) {
        validateKey(key);
        Assert.notNull(messages, "La liste de messages ne peut pas être \"null\"");
        Arrays.stream(messages).forEach(message -> Assert.notNull(message, "Chaque message dans la liste ne peut pas être \"null\""));

        flashes.put(key, List.of(messages));
    }

    @Override
    @Nullable
    public String[] get(String key) {
        return get(key, null);
    }

    @Override
    @Nullable
    public String[] get(String key, @Nullable String[] defaultValue) {
        if (!has(key)) return defaultValue;

        List<String> results = flashes.get(key);
        flashes.remove(key);

        return results.toArray(new String[0]);
    }

    @Override
    @Nullable
    public String[] peek(String key) {
        return peek(key, null);
    }

    @Override
    @Nullable
    public String[] peek(String key, @Nullable String[] defaultValue) {
        return has(key) ? flashes.get(key).toArray(new String[0]) : defaultValue;
    }

    @Override
    public Map<String, String[]> peekAll() {
        Map<String, String[]> results = new HashMap<>();
        flashes.forEach((key, value) -> results.put(key, value.toArray(new String[0])));

        return results;
    }

    @Override
    public Map<String, String[]> all() {
        Map<String, String[]> results = peekAll();
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
