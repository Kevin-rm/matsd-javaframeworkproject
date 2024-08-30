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
    private DatabaseConnector databaseConnector;
    private int maxRows = -1;

    public JdbcTemplate(DatabaseConnector databaseConnector) { this.setDatabaseConnector(databaseConnector); }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    private JdbcTemplate setDatabaseConnector(DatabaseConnector databaseConnector) {
        Assert.notNull(databaseConnector, "Le connecteur à la base de données ne peut pas être \"null\"");

        this.databaseConnector = databaseConnector;
        return this;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        Assert.state(maxRows >= 0, () -> new IllegalArgumentException("maxRows doit être un nombre positif ou nul"));

        this.maxRows = maxRows;
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) throws JdbcException {
        List<T> results;

        Connection connection = databaseConnector.getConnection();
        try {
            results = SQLExecutor.query(connection, sql, rowMapper, -1, maxRows, parameters);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return results;
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws JdbcException {
        List<T> results;

        Connection connection = databaseConnector.getConnection();
        try {
            results = SQLExecutor.query(connection, sql, rowMapper, -1, maxRows);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return results;
    }

    @Override
    public List<Map<String, Object>> queryForMapList(String sql, Object... parameters) throws JdbcException {
        List<Map<String, Object>> results;

        Connection connection = databaseConnector.getConnection();
        try {
            results = SQLExecutor.queryForMapList(connection, sql, maxRows, -1, parameters);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return results;
    }

    @Override
    public List<Map<String, Object>> queryForMapList(String sql) throws JdbcException {
        List<Map<String, Object>> results;

        Connection connection = databaseConnector.getConnection();
        try {
            results = SQLExecutor.queryForMapList(connection, sql, -1, maxRows);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return results;
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... parameters)
        throws JdbcException, NoResultException, NotSingleResultException {
        Map<String, Object> result;

        Connection connection = databaseConnector.getConnection();
        try {
            result = SQLExecutor.queryForMap(connection, sql, maxRows, -1, parameters);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return result;
    }

    @Override
    public Map<String, Object> queryForMap(String sql) throws JdbcException, NoResultException, NotSingleResultException {
        Map<String, Object> result;

        Connection connection = databaseConnector.getConnection();
        try {
            result = SQLExecutor.queryForMap(connection, sql, -1, maxRows);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return result;
    }

    @Override
    public <T> List<T> queryForUniqueColumnList(String sql, Class<T> resultType, Object... parameters)
        throws JdbcException, NonUniqueColumnException {
        List<T> results;

        Connection connection = databaseConnector.getConnection();
        try {
            results = SQLExecutor.queryForUniqueColumnList(connection, sql, resultType, -1, maxRows, parameters);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return results;
    }

    @Override
    public <T> List<T> queryForUniqueColumnList(String sql, Class<T> resultType) throws JdbcException, NonUniqueColumnException {
        List<T> results;

        Connection connection = databaseConnector.getConnection();
        try {
            results = SQLExecutor.queryForUniqueColumnList(connection, sql, resultType, -1, maxRows);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return results;
    }

    @Override
    public <T> T queryForUniqueColumn(String sql, Class<T> resultType, Object... parameters)
        throws JdbcException, NonUniqueColumnException, NoResultException, NotSingleResultException {
        T result;

        Connection connection = databaseConnector.getConnection();
        try {
            result = SQLExecutor.queryForUniqueColumn(connection, sql, resultType, -1, maxRows, parameters);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return result;
    }

    @Override
    public <T> T queryForUniqueColumn(String sql, Class<T> resultType)
        throws JdbcException, NonUniqueColumnException, NoResultException, NotSingleResultException {
        T result;

        Connection connection = databaseConnector.getConnection();
        try {
            result = SQLExecutor.queryForUniqueColumn(connection, sql, resultType, -1, maxRows);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            databaseConnector.releaseConnection(connection);
        }

        return result;
    }

    @Override
    public int update(String sql, Object... parameters) throws JdbcException {
        int rowsAffected;

        Connection connection = databaseConnector.getConnection();
        try {
            connection.setAutoCommit(false);
            rowsAffected = SQLExecutor.update(connection, sql, parameters);
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

    @Override
    public int update(String sql) throws JdbcException {
        int rowsAffected;

        Connection connection = databaseConnector.getConnection();
        try {
            connection.setAutoCommit(false);
            rowsAffected = SQLExecutor.update(connection, sql);
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

    @Override
    public void execute(String sql) throws JdbcException {
        Connection connection = databaseConnector.getConnection();
        try {
            connection.setAutoCommit(false);
            SQLExecutor.execute(connection, sql);
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
    }
}
