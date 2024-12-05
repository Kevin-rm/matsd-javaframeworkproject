package mg.itu.prom16.http;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.Map;

public interface FlashBag {
    void add(String key, Object value);

    void add(String key, Object[] values);

    void set(String key, Object[] values);

    @Nullable
    Object[] get(String key);

    @Nullable
    Object[] get(String key, @Nullable Object[] defaultValue);

    @Nullable
    Object[] peek(String key);

    @Nullable
    Object[] peek(String key, @Nullable Object[] defaultValue);

    Map<String, Object[]> peekAll();

    Map<String, Object[]> all();

    boolean has(String key);
}
