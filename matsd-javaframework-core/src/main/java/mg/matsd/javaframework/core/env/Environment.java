package mg.matsd.javaframework.core.env;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.converter.StringToTypeConverter;

import java.util.HashMap;
import java.util.Map;

public class Environment implements PropertyHolder {
    @Nullable
    private final PropertyHolder propertyHolder;
    @Nullable
    private volatile Map<String, String> properties;

    public Environment(@Nullable PropertyHolder propertyHolder) {
        this.propertyHolder = propertyHolder;
    }

    public Environment() { this(null); }

    @Nullable
    public PropertyHolder getPropertyHolder() {
        return propertyHolder;
    }

    @Override
    public String getSource() {
        return "Environment, System Properties" +
            (propertyHolder != null ? ", " + propertyHolder.getSource() : "");
    }

    @Override
    public boolean has(String key) {
        PropertyHolder.validateKey(key);

        return (propertyHolder != null && propertyHolder.has(key))
            || System.getProperties().containsKey(key)
            || System.getenv().containsKey(key);
    }

    @Override
    public Map<String, String> all() {
        Map<String, String> result = properties;
        if (result == null) {
            synchronized (this) {
                result = properties;
                if (result == null) {
                    Map<String, String> temp = new HashMap<>();
                    if (propertyHolder != null) temp.putAll(propertyHolder.all());
                    System.getProperties().forEach((k, v)
                        -> temp.put(k.toString(), v.toString()));
                    temp.putAll(System.getenv());
                    properties = result = Map.copyOf(temp);
                }
            }
        }

        return result;
    }

    @Override
    @Nullable
    public String get(String key, @Nullable String defaultValue) {
        PropertyHolder.validateKey(key);

        String value = System.getenv(key);
        if (value != null) return value;

        value = System.getProperty(key);
        if (value != null) return value;

        if (propertyHolder != null) {
            value = propertyHolder.get(key, defaultValue);
            if (value != null) return value;
        }

        return defaultValue;
    }

    @Override
    @Nullable
    public String get(String key) {
        return get(key, (String) null);
    }

    @Override
    @Nullable
    public <T> T get(String key, Class<T> expectedType, @Nullable T defaultValue) {
        PropertyHolder.validateKey(key);

        final String envValue = System.getenv(key);
        if (envValue != null) try {
            return StringToTypeConverter.convert(envValue, expectedType);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }

        final String systemPropertyValue = System.getProperty(key);
        if (systemPropertyValue != null) try {
            return StringToTypeConverter.convert(systemPropertyValue, expectedType);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }

        if (propertyHolder != null) {
            T value = propertyHolder.get(key, expectedType, defaultValue);
            if (value != null) return value;
        }

        return defaultValue;
    }

    @Override
    @Nullable
    public <T> T get(String key, Class<T> expectedType) {
        return get(key, expectedType, null);
    }
}