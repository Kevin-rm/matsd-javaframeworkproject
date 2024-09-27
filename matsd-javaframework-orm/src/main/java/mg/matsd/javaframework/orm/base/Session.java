package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.orm.mapping.Entity;
import mg.matsd.javaframework.orm.query.Query;

import java.sql.Connection;
import java.util.List;

public interface Session extends AutoCloseable {
    List<Entity> getEntities();

    Entity getEntity(Class<?> entityClass) throws EntityNotFoundException;

    boolean isEntity(@Nullable Class<?> clazz);

    Connection connection();

    Transaction getTransaction();

    Transaction beginTransaction();

    boolean isOpen();

    Query<?> createQuery(String sql);

    <T> Query<T> createQuery(String sql, Class<T> entityResultClass);

    @Override
    void close();
}
