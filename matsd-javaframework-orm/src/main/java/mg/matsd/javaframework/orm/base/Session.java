package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.query.RawQuery;

import java.sql.Connection;

public interface Session extends AutoCloseable {
    Connection connection();

    Transaction getTransaction();

    Transaction beginTransaction();

    boolean isOpen();

    RawQuery createRawQuery(String sql);

    RawQuery createRawQuery(String sql, Class<?> entityResultClass);

    @Override
    void close();
}
