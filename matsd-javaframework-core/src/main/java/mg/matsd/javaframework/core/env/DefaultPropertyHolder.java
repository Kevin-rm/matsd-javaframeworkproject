package mg.matsd.javaframework.core.env;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.converter.StringToTypeConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class DefaultPropertyHolder implements PropertyHolder {
    private String     source;
    private String[]   locations;
    private Properties properties;

    public DefaultPropertyHolder(String source) {
        this.setSource(source)
            .setLocations()
            .setProperties();
    }

    @Override
    public String getSource() {
        return source;
    }

    public DefaultPropertyHolder setSource(String source) {
        Assert.notBlank(source, false, "La chaîne de caractère contenant " +
            "les emplacements des sources de propriétés ne peut pas être vide ou \"null\"");

        this.source = source.strip();
        return this;
    }

    public String[] getLocations() {
        return locations;
    }

    private DefaultPropertyHolder setLocations() {
        locations = source.split(",");
        return this;
    }

    @Override
    public boolean has(String key) {
        PropertyHolder.validateKey(key);
        return properties.containsKey(key);
    }

    @Override
    public Map<String, String> all() {
        return Map.copyOf(properties.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()
            )));
    }

    @Override
    @Nullable
    public String get(String key, @Nullable String defaultValue) {
        PropertyHolder.validateKey(key);
        return properties.getProperty(key, defaultValue);
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

        final String value = properties.getProperty(key);
        if (value == null) return defaultValue;

        return StringToTypeConverter.convert(value, expectedType);
    }

    @Override
    @Nullable
    public <T> T get(String key, Class<T> expectedType) {
        return get(key, expectedType, null);
    }

    private DefaultPropertyHolder setProperties() {
        properties = new Properties();

        Arrays.stream(locations).forEachOrdered(location -> {
            try (Resource resource = new ClassPathResource(location)) {
                properties.load(resource.getInputStream());
            } catch (IOException e) { throw new RuntimeException(e); }
        });

        return this;
    }
}
