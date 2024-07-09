package mg.itu.prom16.http;

import mg.matsd.javaframework.core.annotations.Nullable;

public interface Session {
    Object get(String key);

    Object get(String key, @Nullable Object defaultValue);

    boolean has(String key);

    void put(String key, @Nullable Object value);

    void remove(String key);

    void invalidate();
}
