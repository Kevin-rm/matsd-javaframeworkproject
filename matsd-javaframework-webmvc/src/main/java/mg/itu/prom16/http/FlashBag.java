package mg.itu.prom16.http;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.Map;

public interface FlashBag {
    void set(String key, @Nullable Object value);

    void setAll(Map<String, ?> map);

    @Nullable
    Object get(String key);

    @Nullable
    Object get(String key, @Nullable Object defaultValue);

    @Nullable
    Object peek(String key);

    @Nullable
    Object peek(String key, @Nullable Object defaultValue);

    Map<String, Object> peekAll();

    Map<String, Object> all();

    boolean has(String key);
}
