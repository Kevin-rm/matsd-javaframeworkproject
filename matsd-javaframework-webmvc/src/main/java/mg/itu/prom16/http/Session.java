package mg.itu.prom16.http;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.Map;

public interface Session {
    Object get(String key);

    Object get(String key, @Nullable Object defaultValue);

    Map<String, Object> all();

    boolean has(String key);

    void put(String key, @Nullable Object value);

    void remove(String key);

    void invalidate();

    FlashBag getFlashBag();

    void addFlash(String key, String message);

    void addFlash(String key, String[] messages);
}
