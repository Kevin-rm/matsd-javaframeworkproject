package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.mapping.Entity;

import java.util.*;

public class SessionFactoryOptions {
    private static final Set<String> VALID_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
        "connection.url", "connection.user", "connection.password", "connection.driver_class", "connection.pool_size",
        "show_sql", "format_sql"
    ));

    private final int index;
    @Nullable
    private String name;
    private DatabaseConnector databaseConnector;
    private List<Entity> entities;
    private final Properties properties;

    SessionFactoryOptions(int index, @Nullable String name) {
        this.index = index;
        properties = new Properties();

        this.setName(name)
            .setDatabaseConnector()
            .setEntities();
    }

    int getIndex() {
        return index;
    }

    String getName() {
        return name;
    }

    private SessionFactoryOptions setName(@Nullable String name) {
        if (StringUtils.isBlank(name)) return this;

        this.name = name;
        return this;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    private SessionFactoryOptions setDatabaseConnector() {
        databaseConnector = new DatabaseConnector(
            properties.getProperty("connection.url"),
            properties.getProperty("connection.user"),
            properties.getProperty("connection.password"),
            properties.getProperty("connection.driver_class"),
            properties.getProperty("connection.pool_size")
        );
        properties.keySet().removeIf(key -> key.toString().startsWith("connection"));

        return this;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    private SessionFactoryOptions setEntities() {
        entities = new ArrayList<>();

        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    @Nullable
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    void setProperty(String key, @Nullable String value) throws ConfigurationException {
        validatePropertyKey(key);

        properties.setProperty(key, value);
    }

    private void validatePropertyKey(String key) {
        Assert.state(VALID_PROPERTY_NAMES.contains(key),
            () -> new ConfigurationException(String.format("Le nom de propriété \"%s\" n'est pas valide", key)
        ));

        if (!properties.containsKey(key)) return;

        String message = String.format("La propriété \"%s\" de la \"session factory\"", key);
        if (name == null)
            message += String.format(" à l'indice %d", index);
        else message += String.format(" avec le nom \"%s\"", name);
        message += " a déjà été définie";

        throw new ConfigurationException(message);
    }
}
