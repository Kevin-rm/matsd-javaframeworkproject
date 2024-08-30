package mg.matsd.javaframework.orm.connection;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.exceptions.ConnectorInstantiationException;

public class MysqlConnector extends DatabaseConnector {
    private static final String DRIVER    = "com.mysql.cj.jdbc.Driver";
    private static final int DEFAULT_PORT = 3306;

    public MysqlConnector(
        @Nullable String host, @Nullable Integer port, String dbName, String user, String password, @Nullable Integer poolSize
    ) throws ConnectorInstantiationException {
        super(host, port, dbName, user, password, poolSize, DRIVER);
    }

    public MysqlConnector(
        @Nullable String host, @Nullable String port, String dbName, String user, String password, @Nullable String poolSize
    ) throws ConnectorInstantiationException {
        super(host, port, dbName, user, password, poolSize, DRIVER);
    }

    @Override
    protected DatabaseConnector setPort(@Nullable Integer port) {
        return super.setPort(port == null ? DEFAULT_PORT : port);
    }

    @Override
    protected DatabaseConnector setPort(@Nullable String port) {
        if (port == null || StringUtils.isBlank(port)) return setPort(DEFAULT_PORT);

        return super.setPort(port);
    }

    @Override
    protected String getUrl() {
        return String.format("jdbc:mysql://%s:%d/%s", getHost(), getPort(), getDbName());
    }
}
