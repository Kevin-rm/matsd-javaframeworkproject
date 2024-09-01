package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.connection.MysqlConnector;
import mg.matsd.javaframework.orm.exceptions.DataSourceNotFoundException;
import mg.matsd.javaframework.orm.setup.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EntityManagerFactory implements SessionFactory  {
    private static final Map<String, String> DATASOURCE_PREFIX_MAP = new HashMap<>();

    private Configuration configuration;
    private String dialect;
    private DatabaseConnector databaseConnector;

    static {
        DATASOURCE_PREFIX_MAP.put("mysql",    Configuration.PROPERTIES_KEY_PREFIX + "mysql.");
        DATASOURCE_PREFIX_MAP.put("postgres", Configuration.PROPERTIES_KEY_PREFIX + "postgres.");
        DATASOURCE_PREFIX_MAP.put("oracle",   Configuration.PROPERTIES_KEY_PREFIX + "oracle.");
    }

    public EntityManagerFactory(Configuration configuration, @Nullable String datasourceToUse) {
        this.setConfiguration    (configuration)
            .setDialect          (datasourceToUse)
            .setDatabaseConnector();
    }

    public EntityManagerFactory(Configuration configuration) {
        this(configuration, null);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private EntityManagerFactory setConfiguration(Configuration configuration) {
        Assert.notNull(configuration, "La configuration de la base de données ne peut pas être \"null\"");

        this.configuration = configuration;
        return this;
    }

    @Override
    public String getDialect() {
        return dialect;
    }

    private EntityManagerFactory setDialect(@Nullable String dialect) {
        Set<String> availableDataSources = configuration.getAvailableDatasources();
        if (StringUtils.isBlank(dialect))
            dialect = availableDataSources.iterator().next();

        dialect = dialect.toLowerCase().strip();
        if (!availableDataSources.contains(dialect))
            throw new DataSourceNotFoundException(dialect, availableDataSources);

        this.dialect = dialect;
        return this;
    }

    @Override
    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    private EntityManagerFactory setDatabaseConnector() {
        String datasourcePrefix = DATASOURCE_PREFIX_MAP.get(dialect);

        Properties properties = configuration.getProperties();
        String host     = properties.getProperty(datasourcePrefix + "host");
        String port     = properties.getProperty(datasourcePrefix + "port");
        String dbName   = properties.getProperty(datasourcePrefix + "database-name");
        String user     = properties.getProperty(datasourcePrefix + "username");
        String password = properties.getProperty(datasourcePrefix + "password");
        String poolSize = properties.getProperty(datasourcePrefix + "pool-size");

        switch (dialect) {
            case "mysql":
                databaseConnector = new MysqlConnector(host, port, dbName, user, password, poolSize);
                break;
            default:
                break;
        }

        return this;
    }

    public EntityManager createEntityManager() {
        return new EntityManager(databaseConnector, dialect);
    }

    @Override
    public Session createSession() {
        return createEntityManager();
    }
}
