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

    static void validateKey(String key) {
        Assert.notBlank(key, false, "La clé de la propriété ne peut pas être vide ou \"null\"");
    }
}
