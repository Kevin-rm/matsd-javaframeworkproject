package mg.matsd.javaframework.orm.jdbc;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.orm.base.internal.SQLExecutor;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.exceptions.NoResultException;
import mg.matsd.javaframework.orm.exceptions.NonUniqueColumnException;
import mg.matsd.javaframework.orm.exceptions.NotSingleResultException;
import mg.matsd.javaframework.orm.exceptions.RollbackException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JdbcTemplate implements JdbcOperations {
    private final DatabaseConnector databaseConnector;
    private int maxRows = -1;

    public JdbcTemplate(DatabaseConnector databaseConnector) {
        Assert.notNull(databaseConnector, "Le connecteur à la base de données ne peut pas être \"null\"");

        this.databaseConnector = databaseConnector;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        Assert.isTrue(maxRows >= 0, "maxRows doit être un nombre positif ou nul");

        this.maxRows = maxRows;
    }

    @Override
    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor, Object... parameters) throws JdbcException {
        return executeQuery(connection -> SQLExecutor.query(connection, sql, resultSetExtractor, -1, maxRows, parameters));
    }

    @Override
    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor) throws JdbcException {
        return executeQuery(connection -> SQLExecutor.query(connection, sql, resultSetExtractor, -1, maxRows));
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) throws JdbcException {
        return executeQuery(connection -> SQLExecutor.query(connection, sql, rowMapper, -1, maxRows, parameters));
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws JdbcException {
        return executeQuery(connection -> SQLExecutor.query(connection, sql, rowMapper, -1, maxRows));
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters)
        throws JdbcException, NoResultException, NotSingleResultException  {
        return executeQuery(connection -> SQLExecutor.queryForObject(connection, sql, rowMapper, -1, maxRows, parameters));
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws JdbcException, NoResultException, NotSingleResultException {
        return executeQuery(connection -> SQLExecutor.queryForObject(connection, sql, rowMapper, -1, maxRows));
    }

    @Override
    public List<Map<String, Object>> queryForMapList(String sql, Object... parameters) throws JdbcException {
        return executeQuery(connection -> SQLExecutor.queryForMapList(connection, sql, -1, maxRows, parameters));
    }

    @Override
    public List<Map<String, Object>> queryForMapList(String sql) throws JdbcException {
        return executeQuery(connection -> SQLExecutor.queryForMapList(connection, sql, -1, maxRows));
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... parameters)
        throws JdbcException, NoResultException, NotSingleResultException {
        return executeQuery(connection -> SQLExecutor.queryForMap(connection, sql, -1, maxRows, parameters));
    }

    @Override
    public Map<String, Object> queryForMap(String sql) throws JdbcException, NoResultException, NotSingleResultException {
        return executeQuery(connection -> SQLExecutor.queryForMap(connection, sql, -1, maxRows));
    }

    @Override
    public <T> List<T> queryForUniqueColumnList(String sql, Class<T> resultType, Object... parameters)
        throws JdbcException, NonUniqueColumnException {
        return executeQuery(connection -> SQLExecutor.queryForUniqueColumnList(connection, sql, resultType, -1, maxRows, parameters));
    }

    @Override
    public <T> List<T> queryForUniqueColumnList(String sql, Class<T> resultType) throws JdbcException, NonUniqueColumnException {
        return executeQuery(connection -> SQLExecutor.queryForUniqueColumnList(connection, sql, resultType, -1, maxRows));
    }

    @Override
    public <T> T queryForUniqueColumn(String sql, Class<T> resultType, Object... parameters)
        throws JdbcException, NoResultException, NonUniqueColumnException, NotSingleResultException {
        return executeQuery(connection -> SQLExecutor.queryForUniqueColumn(connection, sql, resultType, -1, maxRows, parameters));
    }

    @Override
    public <T> T queryForUniqueColumn(String sql, Class<T> resultType)
        throws JdbcException, NoResultException, NonUniqueColumnException, NotSingleResultException {
        return executeQuery(connection -> SQLExecutor.queryForUniqueColumn(connection, sql, resultType, -1, maxRows));
    }

    @Override
    public int update(String sql, Object... parameters) throws JdbcException, RollbackException {
        return executeUpdate(connection -> SQLExecutor.update(connection, sql, parameters));
    }

    @Override
    public int update(String sql) throws JdbcException, RollbackException {
        return executeUpdate(connection -> SQLExecutor.update(connection, sql));
    }

    @Override
    public void execute(String sql) throws JdbcException, RollbackException {
        executeUpdate(connection -> {
            SQLExecutor.execute(connection, sql);

            return null;
        });
    }

    private <T> T executeQuery(QueryProcessor<T> queryProcessor) throws JdbcException {
        Connection connection = databaseConnector.getConnection();
        try {
            return queryProcessor.process(connection);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }
    }

    private int executeUpdate(QueryProcessor<Integer> queryProcessor) throws JdbcException, RollbackException {
        int rowsAffected;

        Connection connection = databaseConnector.getConnection();
        try {
            connection.setAutoCommit(false);
            rowsAffected = queryProcessor.process(connection);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RollbackException(ex);
            }
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return rowsAffected;
    }

    @FunctionalInterface
    private interface QueryProcessor<T> {
        T process(Connection connection) throws SQLException;
    }
}
