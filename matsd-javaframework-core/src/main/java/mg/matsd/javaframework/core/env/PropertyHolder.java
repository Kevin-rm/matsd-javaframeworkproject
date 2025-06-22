package mg.matsd.javaframework.core.env;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.Map;

public interface PropertyHolder {
    String getSource();

    boolean has(String key);

    Map<String, String> all();

    @Nullable String get(String key, @Nullable String defaultValue);

    @Nullable String get(String key);

    @Nullable <T> T get(String key, Class<T> expectedType, @Nullable T defaultValue);

    @Nullable <T> T get(String key, Class<T> expectedType);

    default <T> T getOrFail(String key, Class<T> expectedType) {
        T value= get(key, expectedType);
        if (value == null) throw new IllegalArgumentException(String.format(
            "La propriété \"%s\" n'existe pas ou n'est pas définie dans la source \"%s\"", key, getSource()
        ));

        return value;
    }

    default String getOrFail(String key) {
        return getOrFail(key, String.class);
    }

    static void validateKey(String key) {
        Assert.notBlank(key, false, "La clé de la propriété ne peut pas être vide ou \"null\"");
    }
}