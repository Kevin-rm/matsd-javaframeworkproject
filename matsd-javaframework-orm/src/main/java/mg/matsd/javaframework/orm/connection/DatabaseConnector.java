package mg.matsd.javaframework.orm.connection;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseConnector {
    private static final Integer DEFAULT_POOL_SIZE = 10;

    private String  url;
    private String  user;
    private String  password;
    private String  driver;
    private Integer poolSize;
    private final Queue<Connection> availableConnections;
    private final Queue<Connection> usedConnections;

    public DatabaseConnector(
        String url, String user, String password, String driver, @Nullable Integer poolSize
    ) throws ConnectorInstantiationException {
        try {
            this.setUrl     (url)
                .setUser    (user)
                .setPassword(password)
                .setDriver  (driver)
                .setPoolSize(poolSize)
                .configureShutdownCleanup();

            availableConnections = new ConcurrentLinkedQueue<>();
            usedConnections      = new ConcurrentLinkedQueue<>();
        } catch (Exception e) {
            throw new ConnectorInstantiationException(e);
        }
    }

    public DatabaseConnector(
        String url, String user, String password, String driver, @Nullable String poolSize
    ) throws ConnectorInstantiationException {
        try {
            this.setUrl     (url)
                .setUser    (user)
                .setPassword(password)
                .setDriver  (driver)
                .setPoolSize(poolSize)
                .configureShutdownCleanup();

            availableConnections = new ConcurrentLinkedQueue<>();
            usedConnections      = new ConcurrentLinkedQueue<>();
        } catch (Exception e) {
            throw new ConnectorInstantiationException(e);
        }
    }

    public String getUrl() {
        return url;
    }

    private DatabaseConnector setUrl(String url) {
        Assert.notBlank(url, false, "L'URL de la base de données ne peut pas être vide ou \"null\"");

        this.url = url.strip();
        return this;
    }

    public String getUser() {
        return user;
    }

    private DatabaseConnector setUser(String user) {
        Assert.state(user != null && StringUtils.hasText(user),
            () -> new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide ou \"null\"")
        );

        this.user = user.strip();
        return this;
    }

    public String getPassword() {
        return password;
    }

    private DatabaseConnector setPassword(String password) {
        Assert.notNull(password, "Le mot de passe ne peut pas être \"null\"");

        this.password = password;
        return this;
    }

    public String getDriver() {
        return driver;
    }

    private DatabaseConnector setDriver(String driver) {
        Assert.notBlank(driver, false, "Le pilote de la base de données ne peut pas être vide ou \"null\"");

        this.driver = driver.strip();
        return this;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    private DatabaseConnector setPoolSize(@Nullable Integer poolSize) {
        if (poolSize == null) {
            this.poolSize = DEFAULT_POOL_SIZE;
            return this;
        }

        Assert.positive(poolSize, "L'argument poolSize ne peut pas être négatif ou nul");

        this.poolSize = poolSize;
        return this;
    }

    private DatabaseConnector setPoolSize(@Nullable String poolSize) {
        try {
            return setPoolSize(poolSize == null || StringUtils.isBlank(poolSize) ? null : Integer.valueOf(poolSize.strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'argument poolSize doit être un nombre entier positif");
        }
    }

    public synchronized Connection getConnection() {
        Connection conn;

        if (!availableConnections.isEmpty()) conn = availableConnections.poll();
        else  {
            if (usedConnections.size() >= poolSize)
                throw new NoAvailableConnectionException();

            try {
                conn = createConnection();
            } catch (ClassNotFoundException | SQLException e) {
                throw new DatabaseException(e);
            }
        }

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

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        Connection conn;

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
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
