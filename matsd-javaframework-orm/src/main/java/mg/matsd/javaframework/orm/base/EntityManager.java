package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.exceptions.DatabaseException;
import mg.matsd.javaframework.orm.query.Query;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityManager implements Session {
    private final DatabaseConnector databaseConnector;
    private Connection  connection;
    private Transaction transaction;

    EntityManager(final DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
        connection = databaseConnector.getConnection();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Transaction getTransaction() {
        if (transaction == null) transaction = new Transaction(this);

        return transaction;
    }

    @Override
    public Transaction beginTransaction() {
        Transaction transaction = getTransaction();
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
    public Query<?> createQuery(String sql) {
        return new Query<>(this, sql);
    }

    @Override
    public <T> Query<T> createQuery(String sql, Class<T> entityResultClass) {
        return new Query<>(this, sql, entityResultClass);
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
