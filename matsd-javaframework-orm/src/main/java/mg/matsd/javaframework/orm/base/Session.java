package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.query.Query;

import java.sql.Connection;

public interface Session extends AutoCloseable {
    Connection connection();

    Transaction getTransaction();

    Transaction beginTransaction();

    boolean isOpen();

    Query<?> createQuery(String sql);

    <T> Query<T> createQuery(String sql, Class<T> entityResultClass);

    @Override
    void close();
}
