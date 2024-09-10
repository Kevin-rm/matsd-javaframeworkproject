package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;

import java.util.*;

public class SessionFactoryOptions {
    private static final Set<String> VALID_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
        "connection.url", "connection.user", "connection.password", "connection.driver_class", "connection.pool_size",
        "show_sql", "format_sql"
    ));

    private DatabaseConnector databaseConnector;
    private final int index;
    @Nullable
    private String name;
    private final Properties properties;

    SessionFactoryOptions(int index, @Nullable String name) {
        this.index = index;
        setName(name);
        properties = new Properties();
    }

    public DatabaseConnector getDatabaseConnector() {
        if (databaseConnector != null) return databaseConnector;

        databaseConnector = new DatabaseConnector(
            properties.getProperty("connection.url"),
            properties.getProperty("connection.user"),
            properties.getProperty("connection.password"),
            properties.getProperty("connection.driver_class"),
            properties.getProperty("connection.pool_size")
        );
        properties.keySet().removeIf(key -> key.toString().startsWith("connection"));

        return databaseConnector;
    }

    int getIndex() {
        return index;
    }

    String getName() {
        return name;
    }

    private void setName(@Nullable String name) {
        if (StringUtils.isBlank(name)) return;

        this.name = name;
    }

    public Properties getProperties() {
        return properties;
    }

    @Nullable
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    void setProperty(String key, @Nullable String value) {
        validatePropertyKey(key);

        properties.setProperty(key, value);
    }

    private void validatePropertyKey(String key) {
        Assert.state(VALID_PROPERTY_NAMES.contains(key),
            () -> new ConfigurationException(String.format("Le nom de propriété \"%s\" n'est pas valide", key)
        ));

        if (!properties.containsKey(key)) return;

        String message = String.format("Duplication détectée pour la propriété \"%s\" de la \"session factory\"", key);
        if (name == null)
            message += String.format(" à l'indice %d", index);
        else message += String.format(" avec le nom \"%s\"", name);

        throw new ConfigurationException(message);
    }
}
