package mg.matsd.javaframework.orm.jdbc;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.orm.exceptions.NoResultException;
import mg.matsd.javaframework.orm.exceptions.NotSingleResultException;
import mg.matsd.javaframework.orm.exceptions.NonUniqueColumnException;

import java.util.List;
import java.util.Map;

public interface JdbcOperations {
    <T> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object... parameters) throws JdbcException;

    <T> List<T> query(String sql, RowMapper<T> rowMapper) throws JdbcException;

    List<Map<String, Object>> queryForMapList(String sql, @Nullable Object... parameters) throws JdbcException;

    List<Map<String, Object>> queryForMapList(String sql) throws JdbcException;

    Map<String, Object> queryForMap(String sql, @Nullable Object... parameters) throws JdbcException, NoResultException, NotSingleResultException;

    Map<String, Object> queryForMap(String sql) throws JdbcException, NoResultException, NotSingleResultException;

    <T> List<T> queryForUniqueColumnList(String sql, Class<T> resultType, @Nullable Object... parameters) throws JdbcException, NonUniqueColumnException;

    <T> List<T> queryForUniqueColumnList(String sql, Class<T> resultType) throws JdbcException, NonUniqueColumnException;

    <T> T queryForUniqueColumn(String sql, Class<T> resultType, @Nullable Object... parameters) throws JdbcException, NonUniqueColumnException, NoResultException, NotSingleResultException;

    <T> T queryForUniqueColumn(String sql, Class<T> resultType) throws JdbcException, NonUniqueColumnException, NoResultException, NotSingleResultException;

    int update(String sql, @Nullable Object... parameters) throws JdbcException;

    int update(String sql) throws JdbcException;

    void execute(String sql) throws JdbcException;
}
