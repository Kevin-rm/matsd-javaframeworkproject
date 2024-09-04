package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.query.RawQuery;

import java.sql.Connection;

public interface Session extends AutoCloseable {
    Connection connection();

    Transaction getTransaction();

    Transaction beginTransaction();

    boolean isOpen();

    RawQuery<?> createRawQuery(String sql);

    <T> RawQuery<T> createRawQuery(String sql, Class<T> entityResultClass);

    @Override
    void close();
}
