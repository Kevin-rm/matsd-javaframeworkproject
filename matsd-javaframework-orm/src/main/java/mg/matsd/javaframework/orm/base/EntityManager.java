package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.exceptions.DatabaseException;
import mg.matsd.javaframework.orm.query.RawQuery;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityManager implements Session {
    private DatabaseConnector databaseConnector;
    private String            dialect;
    private Connection        connection;

    EntityManager(DatabaseConnector databaseConnector, String dialect) {
        this.databaseConnector = databaseConnector;
        this.dialect           = dialect;
        connection             = databaseConnector.getConnection();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Transaction getTransaction() {
        return new Transaction(this);
    }

    @Override
    public Transaction beginTransaction() {
        Transaction transaction = new Transaction(this);
        transaction.begin();

        return transaction;
    }

    @Override
    public boolean isOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public RawQuery createRawQuery(String sql) {
        return new RawQuery(this, sql);
    }

    @Override
    public RawQuery createRawQuery(String sql, Class<?> entityResultClass) {
        return new RawQuery(this, sql, entityResultClass);
    }

    public void findAll(Class<?> entityClass) {

    }

    public void persist(Object entityObject) {

    }

    @Override
    public void close() {
        databaseConnector.releaseConnection(connection);
        connection = null;
    }
}
