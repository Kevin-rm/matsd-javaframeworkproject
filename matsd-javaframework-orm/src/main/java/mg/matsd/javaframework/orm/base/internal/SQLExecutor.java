package mg.matsd.javaframework.orm.base.internal;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.orm.exceptions.*;
import mg.matsd.javaframework.orm.jdbc.*;

import java.sql.*;
import java.util.*;

import static java.sql.Types.NULL;

public class SQLExecutor {
    private static final Set<String> DDL_QUERY_STARTS;
    private static final Set<String> DML_QUERY_STARTS;

    static {
        DDL_QUERY_STARTS = new HashSet<>();
        DDL_QUERY_STARTS.add("CREATE");
        DDL_QUERY_STARTS.add("ALTER");
        DDL_QUERY_STARTS.add("DROP");

        DML_QUERY_STARTS = new HashSet<>();
        DML_QUERY_STARTS.add("INSERT");
        DML_QUERY_STARTS.add("UPDATE");
        DML_QUERY_STARTS.add("DELETE");
    }

    private SQLExecutor() { }

    public static <T> List<T> query(
        Connection connection, String sql, RowMapper<T> rowMapper, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException {
        validateDQLQuery(sql);

        List<T> results;
        try (PreparedStatement preparedStatement = connection.prepareStatement(
            sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            setMaxRows   (preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);
                results = mapResultSet(resultSet, rowMapper);
            }
        }
        return results;
    }

    public static <T> List<T> query(
        Connection connection, String sql, RowMapper<T> rowMapper, int startRow, int maxRows
    ) throws SQLException {
        validateDQLQuery(sql);

        List<T> results;
        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                results = mapResultSet(resultSet, rowMapper);
            }
        }

        return results;
    }

    public static List<Map<String, Object>> queryForMapList(
        Connection connection, String sql, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException {
        validateDQLQuery(sql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(
            sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            setMaxRows   (preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);
                return resultSetToMapList(resultSet);
            }
        }
    }

    public static List<Map<String, Object>> queryForMapList(Connection connection, String sql, int startRow, int maxRows) throws SQLException {
        validateDQLQuery(sql);

        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                return resultSetToMapList(resultSet);
            }
        }
    }

    public static Map<String, Object> queryForMap(
        Connection connection, String sql, int start, int maxRows, @Nullable Object... parameters
    ) throws SQLException, NoResultException, NotSingleResultException {
        List<Map<String, Object>> results = queryForMapList(connection, sql, start, maxRows, parameters);

        if (results.isEmpty())  throw new NoResultException(sql);
        if (results.size() > 1) throw new NotSingleResultException(sql);

        return results.get(0);
    }

    public static Map<String, Object> queryForMap(Connection connection, String sql, int startRow, int maxRows)
        throws SQLException, NoResultException, NotSingleResultException {
        List<Map<String, Object>> results = queryForMapList(connection, sql, startRow, maxRows);

        if (results.isEmpty())  throw new NoResultException(sql);
        if (results.size() > 1) throw new NotSingleResultException(sql);

        return results.get(0);
    }

    public static <T> List<T> queryForUniqueColumnList(
        Connection connection, String sql, Class<T> resultType, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException, NonUniqueColumnException {
        validateDQLQuery(sql);
        Assert.state(ClassUtils.isStandardClass(resultType),
            () -> new IllegalArgumentException(String.format("La classe \"%s\" n'est pas une classe standard de JAVA", resultType.getName()))
        );

        try (PreparedStatement preparedStatement = connection.prepareStatement(
            sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            setMaxRows   (preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);
                return resultSetToUniqueColumnList(resultSet, resultType, sql);
            }
        }
    }

    public static <T> List<T> queryForUniqueColumnList(Connection connection, String sql, Class<T> resultType, int startRow, int maxRows)
        throws SQLException, NonUniqueColumnException {
        validateDQLQuery(sql);
        Assert.state(ClassUtils.isStandardClass(resultType),
            () -> new IllegalArgumentException(String.format("La classe \"%s\" n'est pas une classe standard de JAVA", resultType.getName()))
        );

        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                return resultSetToUniqueColumnList(resultSet, resultType, sql);
            }
        }
    }

    public static <T> T queryForUniqueColumn(
        Connection connection, String sql, Class<T> resultType, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException, NonUniqueColumnException, NoResultException, NotSingleResultException {
        List<T> results = queryForUniqueColumnList(connection, sql, resultType, startRow, maxRows, parameters);

        if (results.isEmpty())  throw new NoResultException(sql);
        if (results.size() > 1) throw new NotSingleResultException(sql);

        return results.get(0);
    }

    public static <T> T queryForUniqueColumn(Connection connection, String sql, Class<T> resultType, int startRow, int maxRows)
        throws SQLException, NonUniqueColumnException, NoResultException, NotSingleResultException {
        List<T> results = queryForUniqueColumnList(connection, sql, resultType, startRow, maxRows);

        if (results.isEmpty())  throw new NoResultException(sql);
        if (results.size() > 1) throw new NotSingleResultException(sql);

        return results.get(0);
    }

    public static int update(Connection connection, String sql, @Nullable Object... parameters) throws SQLException {
        validateDMLQuery(sql);

        int rowsAffected;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, parameters);

            rowsAffected = preparedStatement.executeUpdate();
        }

        return rowsAffected;
    }

    public static int update(Connection connection, String sql) throws SQLException {
        validateDMLQuery(sql);

        int rowsAffected;
        try (Statement statement = connection.createStatement()) {
            rowsAffected = statement.executeUpdate(sql);
        }

        return rowsAffected;
    }

    public static void execute(Connection connection, String sql) throws SQLException {
        validateDDLQuery(sql);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static void validateDQLQuery(String sql) {
        Assert.notBlank(sql, false, "La requête SQL ne peut pas être vide ou \"null\"");

        String[] sqlParts = sql.strip().split("\\s+");
        Assert.state(sqlParts.length >= 2,
            () -> new BadQueryException("Requête trop courte. Une requête \"DQL (SELECT)\" doit comporter au moins 2 mots")
        );
        Assert.state(sqlParts[0].equalsIgnoreCase("SELECT"),
            () -> new BadQueryException("La requête fournie n'est pas une requête DQL (SELECT)")
        );
    }

    private static void validateDMLQuery(String sql) {
        Assert.notBlank(sql, false, "La requête SQL ne peut pas être vide ou \"null\"");

        String[] sqlParts = sql.strip().split("\\s+");
        Assert.state(sqlParts.length >= 3,
            () -> new BadQueryException(String.format("Requête trop courte. Une requête \"DML %s\" doit comporter au moins 3 mots", DML_QUERY_STARTS))
        );
        Assert.state(DML_QUERY_STARTS.contains(sqlParts[0].toUpperCase()),
            () -> new BadQueryException(String.format("La requête fournie n'est pas une requête DML %s", DML_QUERY_STARTS))
        );
    }

    private static void validateDDLQuery(String sql) {
        Assert.notBlank(sql, false, "La requête SQL ne peut pas être vide ou \"null\"");

        String[] sqlParts = sql.strip().split("\\s+");
        Assert.state(sqlParts.length >= 3,
            () -> new BadQueryException(String.format("Requête trop courte. Une requête \"DDL %s\" doit comporter au moins 3 mots", DDL_QUERY_STARTS))
        );
        Assert.state(DDL_QUERY_STARTS.contains(sqlParts[0].toUpperCase()),
            () -> new BadQueryException(String.format("La requête fournie n'est pas une requête DDL %s", DDL_QUERY_STARTS))
        );
    }

    private static void setStartRow(ResultSet resultSet, int startRow) throws SQLException {
        if (startRow > 0) resultSet.absolute(startRow);
    }

    private static void setMaxRows(PreparedStatement preparedStatement, int maxRows) throws SQLException {
        if (maxRows > -1) preparedStatement.setMaxRows(maxRows);
    }

    private static void setMaxRows(Statement statement, int maxRows) throws SQLException {
        if (maxRows > -1) statement.setMaxRows(maxRows);
    }

    private static void setParameters(PreparedStatement preparedStatement, @Nullable Object... parameters) throws SQLException {
        int index = 1;
        if (parameters == null) preparedStatement.setObject(index, NULL);
        else for (Object parameter : parameters)
            preparedStatement.setObject(index++, parameter);
    }

    private static <T> List<T> mapResultSet(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next())
            results.add(rowMapper.mapRow(resultSet));

        return results;
    }

    private static List<Map<String, Object>> resultSetToMapList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++)
                row.put(metaData.getColumnName(i), resultSet.getObject(i));

            results.add(row);
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> resultSetToUniqueColumnList(ResultSet resultSet, Class<T> resultType, String sql) throws SQLException, NonUniqueColumnException {
        List<T> results = new ArrayList<>();

        while (resultSet.next()) {
            if (resultSet.getMetaData().getColumnCount() != 1)
                throw new NonUniqueColumnException(sql);

            if (resultType == Object.class)
                results.add((T) resultSet.getObject(1));
            else results.add(resultSet.getObject(1, resultType));
        }

        return results;
    }
}
