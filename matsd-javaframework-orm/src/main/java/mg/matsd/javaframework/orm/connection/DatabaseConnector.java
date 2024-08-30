package mg.matsd.javaframework.orm.connection;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.exceptions.ConnectorInstantiationException;
import mg.matsd.javaframework.orm.exceptions.DatabaseException;
import mg.matsd.javaframework.orm.exceptions.NoAvailableConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class DatabaseConnector {
    private static final Integer DEFAULT_POOL_SIZE = 10;

    private String  host;
    private Integer port;
    private String  dbName;
    private String  user;
    private String  password;
    private Integer poolSize;
    private String  driver;
    private List<Connection> availableConnections;
    private List<Connection> usedConnections;

    protected DatabaseConnector(
        @Nullable String host, Integer port, String dbName, String user, String password, @Nullable Integer poolSize, String driver
    ) throws ConnectorInstantiationException {
        try {
            this.setHost    (host)
                .setPort    (port)
                .setDbName  (dbName)
                .setUser    (user)
                .setPassword(password)
                .setPoolSize(poolSize)
                .setDriver  (driver)
                .setAvailableConnections()
                .setUsedConnections()
                .configureShutdownCleanup();
        } catch (Exception e) {
            throw new ConnectorInstantiationException(e);
        }
    }

    protected DatabaseConnector(
        @Nullable String host, String port, String dbName, String user, String password, @Nullable String poolSize, String driver
    ) throws ConnectorInstantiationException {
        try {
            this.setHost    (host)
                .setPort    (port)
                .setDbName  (dbName)
                .setUser    (user)
                .setPassword(password)
                .setPoolSize(poolSize)
                .setDriver  (driver)
                .setAvailableConnections()
                .setUsedConnections()
                .configureShutdownCleanup();
        } catch (Exception e) {
            throw new ConnectorInstantiationException(e);
        }
    }

    public String getHost() {
        return host;
    }

    protected DatabaseConnector setHost(@Nullable String host) {
        if (host == null || StringUtils.isBlank(host)) host = "localhost";

        this.host = host.strip();
        return this;
    }

    public Integer getPort() {
        return port;
    }

    protected DatabaseConnector setPort(Integer port) {
        Assert.notNull(port, "Le numéro de port ne peut pas être \"null\"");
        Assert.inRange(port, 1, 65535, "Le numéro de port spécifié doit être compris entre 1 et 65535");

        this.port = port;
        return this;
    }

    protected DatabaseConnector setPort(String port) {
        Assert.notBlank(port, false, "Le numéro de port ne peut pas être vide ou \"null\"");

        try {
            return setPort(Integer.valueOf(port.strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le numéro de port doit être un nombre entier positif");
        }
    }

    public String getDbName() {
        return dbName;
    }

    protected DatabaseConnector setDbName(String dbName) {
        Assert.notBlank(dbName, false, "Le nom de la base de données ne peut pas être vide ou \"null\"");

        this.dbName = dbName.strip();
        return this;
    }

    public String getUser() {
        return user;
    }

    protected DatabaseConnector setUser(String user) {
        Assert.state(user != null && StringUtils.hasText(user),
            () -> new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide ou \"null\"")
        );

        this.user = user.strip();
        return this;
    }

    public String getPassword() {
        return password;
    }

    protected DatabaseConnector setPassword(String password) {
        Assert.notNull(password, "Le mot de passe ne peut pas être \"null\"");

        this.password = password;
        return this;
    }

    public String getDriver() {
        return driver;
    }

    protected DatabaseConnector setDriver(String driver) {
        Assert.notBlank(driver, false, "Le pilote de la base de données ne peut pas être vide ou \"null\"");

        this.driver = driver.strip();
        return this;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    protected DatabaseConnector setPoolSize(@Nullable Integer poolSize) {
        if (poolSize == null) poolSize = DEFAULT_POOL_SIZE;
        Assert.positive(poolSize, "L'argument poolSize ne peut pas être négatif ou nul");

        this.poolSize = poolSize;
        return this;
    }

    protected DatabaseConnector setPoolSize(@Nullable String poolSize) {
        try {
            return setPoolSize(poolSize == null || StringUtils.isBlank(poolSize) ? null : Integer.valueOf(poolSize.strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'argument poolSize doit être un nombre entier positif");
        }
    }

    protected DatabaseConnector setAvailableConnections() throws SQLException, ClassNotFoundException {
        availableConnections = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++)
            availableConnections.add(createConnection());

        return this;
    }

    protected DatabaseConnector setUsedConnections() {
        usedConnections = new ArrayList<>(poolSize);
        return this;
    }

    public synchronized Connection getConnection() {
        if (availableConnections.isEmpty()) throw new NoAvailableConnectionException();

        Connection conn = availableConnections.remove(availableConnections.size() - 1);
        usedConnections.add(conn);

        return conn;
    }

    public synchronized void releaseConnection(Connection connection) {
        Assert.state(usedConnections.contains(connection),
            () -> new IllegalArgumentException("La connexion fournie n'est pas dans le pool de connexions utilisées")
        );

        try {
            Assert.state(!connection.isClosed(), "La connexion retournée au pool ne doit pas être fermée");

            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        usedConnections.remove(connection);
        availableConnections.add(connection);
    }

    protected abstract String getUrl();

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        Connection conn;

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(getUrl(), user, password);
            conn.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException(String.format("Le pilote JDBC \"%s\" n'a pas été trouvé", driver));
        } catch (SQLException e) {
            throw new SQLException("Échec de la connexion à la base de données", e);
        }

        return conn;
    }

    private void closeConnections() {
        try {
            for (Connection conn : availableConnections) conn.close();
            for (Connection conn : usedConnections)
                if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur durant la fermeture des connexions", e);
        } finally {
            availableConnections.clear();
            usedConnections.clear();
        }
    }

    private void configureShutdownCleanup() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeConnections));
    }
}
