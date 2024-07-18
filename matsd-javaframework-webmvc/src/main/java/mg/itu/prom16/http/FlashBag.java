package mg.itu.prom16.http;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.Map;

public interface FlashBag {
    void add(String key, String message);

    void add(String key, String[] messages);

    void set(String key, String[] messages);

    @Nullable
    String[] get(String key);

    @Nullable
    String[] get(String key, @Nullable String[] defaultValue);

    @Nullable
    String[] peek(String key);

    @Nullable
    String[] peek(String key, @Nullable String[] defaultValue);

    Map<String, String[]> peekAll();

    Map<String, String[]> all();

    boolean has(String key);
}
