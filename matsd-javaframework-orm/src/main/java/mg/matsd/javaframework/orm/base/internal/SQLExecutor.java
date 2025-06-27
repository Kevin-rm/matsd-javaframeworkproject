package mg.matsd.javaframework.orm.base.internal;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.orm.exceptions.*;
import mg.matsd.javaframework.orm.jdbc.*;

import java.sql.*;
import java.util.*;

import static java.sql.Types.NULL;

public final class SQLExecutor {
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

    public static <T> T query(
        Connection connection, String sql, ResultSetExtractor<T> resultSetExtractor, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException {
        validateDQLQuery(sql);

        try (PreparedStatement preparedStatement = createScrollablePreparedStatement(connection, sql)) {
            setMaxRows(preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);
                return resultSetExtractor.extractData(resultSet);
            }
        }
    }

    public static <T> T query(Connection connection, String sql, ResultSetExtractor<T> resultSetExtractor, int startRow, int maxRows)
        throws SQLException {
        validateDQLQuery(sql);

        try (Statement statement = createScrollableStatement(connection)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                return resultSetExtractor.extractData(resultSet);
            }
        }
    }

    public static <T> List<T> query(
        Connection connection, String sql, RowMapper<T> rowMapper, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException {
        validateDQLQuery(sql);

        List<T> results;
        try (PreparedStatement preparedStatement = createScrollablePreparedStatement(connection, sql)) {
            setMaxRows   (preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);
                results = resultSetToGenericList(resultSet, rowMapper);
            }
        }

        return results;
    }

    public static <T> List<T> query(
        Connection connection, String sql, RowMapper<T> rowMapper, int startRow, int maxRows
    ) throws SQLException {
        validateDQLQuery(sql);

        List<T> results;
        try (Statement statement = createScrollableStatement(connection)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                results = resultSetToGenericList(resultSet, rowMapper);
            }
        }

        return results;
    }

    public static <T> T queryForObject(
        Connection connection, String sql, RowMapper<T> rowMapper, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException, NoResultException, NotSingleResultException {
        validateDQLQuery(sql);

        try (PreparedStatement preparedStatement = createScrollablePreparedStatement(connection, sql)) {
            setMaxRows(preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);

                T result;
                if (!resultSet.next()) throw new NoResultException(sql);
                result = rowMapper.mapRow(resultSet);
                if (resultSet.next()) throw new NotSingleResultException(sql);

                return result;
            }
        }
    }

    public static <T> T queryForObject(
        Connection connection, String sql, RowMapper<T> rowMapper, int startRow, int maxRows
    ) throws SQLException, NoResultException, NotSingleResultException {
        validateDQLQuery(sql);

        try (Statement statement = createScrollableStatement(connection)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);

                T result;
                if (!resultSet.next()) throw new NoResultException(sql);
                result = rowMapper.mapRow(resultSet);
                if (resultSet.next()) throw new NotSingleResultException(sql);

                return result;
            }
        }
    }

    public static List<Map<String, Object>> queryForMapList(
        Connection connection, String sql, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException {
        validateDQLQuery(sql);

        try (PreparedStatement preparedStatement = createScrollablePreparedStatement(connection, sql)) {
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

        try (Statement statement = createScrollableStatement(connection)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                return resultSetToMapList(resultSet);
            }
        }
    }

    public static Map<String, Object> queryForMap(
        Connection connection, String sql, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException, NoResultException, NotSingleResultException {
        validateDQLQuery(sql);

        try (PreparedStatement preparedStatement = createScrollablePreparedStatement(connection, sql)) {
            setMaxRows(preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);
                return resultSetToMap(resultSet, sql);
            }
        }
    }

    public static Map<String, Object> queryForMap(
        Connection connection, String sql, int startRow, int maxRows
    ) throws SQLException, NoResultException, NotSingleResultException {
        validateDQLQuery(sql);

        try (Statement statement = createScrollableStatement(connection)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                return resultSetToMap(resultSet, sql);
            }
        }
    }

    public static <T> List<T> queryForUniqueColumnList(
        Connection connection, String sql, Class<T> resultType, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException, NonUniqueColumnException {
        validationForUniqueColumn(sql, resultType);

        try (PreparedStatement preparedStatement = createScrollablePreparedStatement(connection, sql)) {
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
        validationForUniqueColumn(sql, resultType);

        try (Statement statement = createScrollableStatement(connection)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);
                return resultSetToUniqueColumnList(resultSet, resultType, sql);
            }
        }
    }

    public static <T> T queryForUniqueColumn(
        Connection connection, String sql, Class<T> resultType, int startRow, int maxRows, @Nullable Object... parameters
    ) throws SQLException, NoResultException, NonUniqueColumnException, NotSingleResultException {
        validationForUniqueColumn(sql, resultType);

        try (PreparedStatement preparedStatement = createScrollablePreparedStatement(connection, sql)) {
            setMaxRows(preparedStatement, maxRows);
            setParameters(preparedStatement, parameters);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                setStartRow(resultSet, startRow);

                if (!resultSet.next()) throw new NoResultException(sql);
                T result = resultSetToUniqueColumn(resultSet, resultType, sql);
                if (resultSet.next()) throw new NotSingleResultException(sql);

                return result;
            }
        }
    }

    public static <T> T queryForUniqueColumn(
        Connection connection, String sql, Class<T> resultType, int startRow, int maxRows
    ) throws SQLException, NoResultException, NonUniqueColumnException, NotSingleResultException {
        validationForUniqueColumn(sql, resultType);

        try (Statement statement = createScrollableStatement(connection)) {
            setMaxRows(statement, maxRows);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                setStartRow(resultSet, startRow);

                if (!resultSet.next()) throw new NoResultException(sql);
                T result = resultSetToUniqueColumn(resultSet, resultType, sql);
                if (resultSet.next()) throw new NotSingleResultException(sql);

                return result;
            }
        }
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
        Assert.isTrue(sqlParts.length >= 2,
            () -> new BadQueryException("Requête trop courte. Une requête \"DQL (SELECT)\" doit comporter au moins 2 mots")
        );
        Assert.isTrue(sqlParts[0].equalsIgnoreCase("SELECT"),
            () -> new BadQueryException("La requête fournie n'est pas une requête DQL (SELECT)")
        );
    }

    private static void validateDMLQuery(String sql) {
        Assert.notBlank(sql, false, "La requête SQL ne peut pas être vide ou \"null\"");

        String[] sqlParts = sql.strip().split("\\s+");
        Assert.isTrue(sqlParts.length >= 3,
            () -> new BadQueryException(String.format("Requête trop courte. Une requête \"DML %s\" doit comporter au moins 3 mots", DML_QUERY_STARTS))
        );
        Assert.isTrue(DML_QUERY_STARTS.contains(sqlParts[0].toUpperCase()),
            () -> new BadQueryException(String.format("La requête fournie n'est pas une requête DML %s", DML_QUERY_STARTS))
        );
    }

    private static void validateDDLQuery(String sql) {
        Assert.notBlank(sql, false, "La requête SQL ne peut pas être vide ou \"null\"");

        String[] sqlParts = sql.strip().split("\\s+");
        Assert.isTrue(sqlParts.length >= 3,
            () -> new BadQueryException(String.format("Requête trop courte. Une requête \"DDL %s\" doit comporter au moins 3 mots", DDL_QUERY_STARTS))
        );
        Assert.isTrue(DDL_QUERY_STARTS.contains(sqlParts[0].toUpperCase()),
            () -> new BadQueryException(String.format("La requête fournie n'est pas une requête DDL %s", DDL_QUERY_STARTS))
        );
    }

    private static void validationForUniqueColumn(String sql, Class<?> resultType) {
        validateDQLQuery(sql);
        Assert.isTrue(ClassUtils.isStandardClass(resultType),
            () -> new IllegalArgumentException(String.format("La classe \"%s\" n'est pas une classe standard de JAVA", resultType.getName()))
        );
    }

    private static void setStartRow(ResultSet resultSet, int startRow) throws SQLException {
        if (startRow > 0) resultSet.absolute(startRow);
    }

    private static void setMaxRows(Statement statement, int maxRows) throws SQLException {
        if (maxRows > -1) statement.setMaxRows(maxRows);
    }

    private static PreparedStatement createScrollablePreparedStatement(Connection connection, String sql) throws SQLException {
        return connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    private static Statement createScrollableStatement(Connection connection) throws SQLException {
        return connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    private static void setParameters(PreparedStatement preparedStatement, @Nullable Object... parameters) throws SQLException {
        if (parameters == null) {
            preparedStatement.setNull(1, NULL);
            return;
        }

        ResultSetMetaData resultSetMetaData = preparedStatement.getMetaData();
        for (int i = 0; i < parameters.length; i++) {
            int parameterIndex = i + 1;
            Object parameter = parameters[i];

            if (parameter == null)
                preparedStatement.setNull(parameterIndex, resultSetMetaData.getColumnType(parameterIndex));
            else preparedStatement.setObject(parameterIndex, parameter);
        }
    }

    private static <T> List<T> resultSetToGenericList(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next())
            results.add(rowMapper.mapRow(resultSet));

        return results;
    }

    private static Map<String, Object> convertResultSetRowToMap(
        ResultSet resultSet, @Nullable ResultSetMetaData resultSetMetaData, @Nullable Integer columnCount
    ) throws SQLException {
        if (resultSetMetaData == null) resultSetMetaData = resultSet.getMetaData();
        if (columnCount == null)       columnCount = resultSetMetaData.getColumnCount();

        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 1; i <= columnCount; i++)
            map.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i));

        return map;
    }

    private static Map<String, Object> resultSetToMap(ResultSet resultSet, String sql)
        throws SQLException, NoResultException, NotSingleResultException {
        if (!resultSet.next()) throw new NoResultException(sql);
        Map<String, Object> result = convertResultSetRowToMap(resultSet, null, null);
        if (resultSet.next())  throw new NotSingleResultException(sql);

        return result;
    }

    private static List<Map<String, Object>> resultSetToMapList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();

        while (resultSet.next())
            results.add(convertResultSetRowToMap(resultSet, resultSetMetaData, columnCount));

        return results;
    }

    @SuppressWarnings("unchecked")
    private static <T> T resultSetToUniqueColumn(ResultSet resultSet, Class<T> resultType, String sql) throws SQLException, NonUniqueColumnException {
        if (resultSet.getMetaData().getColumnCount() != 1)
            throw new NonUniqueColumnException(sql);

        T result;
        if (resultType == Object.class)
             result = (T) resultSet.getObject(1);
        else result = resultSet.getObject(1, resultType);

        return result;
    }

    private static <T> List<T> resultSetToUniqueColumnList(ResultSet resultSet, Class<T> resultType, String sql)
        throws SQLException, NonUniqueColumnException {
        List<T> results = new ArrayList<>();
        while (resultSet.next())
            results.add(resultSetToUniqueColumn(resultSet, resultType, sql));

        return results;
    }
}
