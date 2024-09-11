package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassScanner;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringConverter;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.mapping.Entity;

import java.util.*;

public class SessionFactoryOptions {
    private static final Set<String> VALID_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
        "connection.url", "connection.user", "connection.password", "connection.driver_class", "connection.pool_size",
        "show_sql", "format_sql", "entity_scan.package"
    ));

    private final Properties properties;
    private final int index;
    @Nullable
    private final String name;
    private DatabaseConnector databaseConnector;
    private boolean showSql;
    private boolean formatSql;
    private String  entityScanPackage;
    private List<Entity> entities;

    SessionFactoryOptions(int index, @Nullable String name) {
        this.index = index;
        this.name = StringUtils.isBlank(name) ? null : name;
        properties = new Properties();
    }

    @Nullable
    public String getProperty(String key) {
        String value = properties.getProperty(key);

        return value == null || StringUtils.isBlank(value) ? null : value;
    }

    void setProperty(String key, @Nullable String value) throws ConfigurationException {
        validatePropertyKey(key);

        properties.setProperty(key, value);
    }

    String getName() {
        return name;
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

        return this;
    }

    public boolean isShowSql() {
        return showSql;
    }

    private SessionFactoryOptions setShowSql() {
        showSql = getBooleanProperty("show_sql");
        return this;
    }

    public boolean isFormatSql() {
        return formatSql;
    }

    private SessionFactoryOptions setFormatSql() {
        formatSql = getBooleanProperty("format_sql");
        return this;
    }

    public String getEntityScanPackage() {
        return entityScanPackage;
    }

    void setEntityScanPackage(String entityScanPackage) {
        if (this.entityScanPackage != null) return;

        Assert.notBlank(entityScanPackage, false, "Le nom de package des entités à scanner ne peut pas être vide ou \"null\"");
        Assert.state(StringUtils.isValidPackageName(entityScanPackage),
            () -> new ConfigurationException(String.format("Le nom de package des entités à scanner fourni \"%s\" n'est pas valide", entityScanPackage))
        );

        this.entityScanPackage = entityScanPackage;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    private SessionFactoryOptions setEntities() {
        Assert.state(entityScanPackage != null, "Le nom de package des entités à scanner n'a pas été défini");

        entities = new ArrayList<>();
        ClassScanner.doScan(entityScanPackage, clazz -> {
            if (!UtilFunctions.isEntity(clazz)) return;
            entities.add(new Entity(clazz));
        });

        return this;
    }

    SessionFactoryOptions configure() {
        return setDatabaseConnector()
            .setEntities()
            .setShowSql()
            .setFormatSql();
    }

    private boolean getBooleanProperty(String key) {
        String value = getProperty(key);
        if (value == null) return false;

        try {
            return StringConverter.convert(value, boolean.class);
        } catch (TypeMismatchException e) {
            throw new ConfigurationException(String.format("La valeur de la propriété \"%s\" fournie " +
                "n'est pas de type boolean : \"%s\"", key, value));
        }
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
