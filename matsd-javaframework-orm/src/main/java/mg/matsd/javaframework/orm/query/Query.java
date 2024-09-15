package mg.matsd.javaframework.orm.query;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.orm.base.Session;
import mg.matsd.javaframework.orm.base.internal.SQLExecutor;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;
import mg.matsd.javaframework.orm.exceptions.DatabaseException;
import mg.matsd.javaframework.orm.exceptions.NoResultException;
import mg.matsd.javaframework.orm.exceptions.NonUniqueColumnException;
import mg.matsd.javaframework.orm.exceptions.NotSingleResultException;
import mg.matsd.javaframework.orm.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Query<T> {
    private final Session session;
    private final String originalSql;
    private String sql;
    @Nullable
    private Class<T> resultClass;
    private int firstResult = -1;
    private int maxResults  = -1;
    private final List<QueryParameter> parameters;

    public Query(Session session, String sql, @Nullable Class<T> resultClass) {
        Assert.notNull(session, "La session ne peut pas être \"null\"");

        this.setSql(sql)
            .setResultClass(resultClass);

        this.session = session;
        originalSql  = this.sql;
        parameters   = new ArrayList<>();
    }

    public Query(Session session, String sql) {
        this(session, sql, null);
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public String getSql() {
        return sql;
    }

    Query<T> setSql(String sql) {
        Assert.notBlank(sql, false, "La requête SQL ne peut pas être vide ou \"null\"");

        this.sql = sql.strip();
        return this;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    private Query<T> setResultClass(@Nullable Class<T> resultClass) {
        if (resultClass == null) return this;
        if (
            resultClass.isArray()              ||
            resultClass.isAnnotation()         ||
            ClassUtils.isAbstract(resultClass) ||
            resultClass.isInterface()          ||
            resultClass.isAnonymousClass()     ||
            !ClassUtils.isPublic(resultClass)
        )
            throw new IllegalArgumentException(
                "La valeur de l'argument resultClass ne doit pas être : " +
                "un tableau, une annotation, une classe abstraite(Les types primitifs y compris), une interface ou une classe anonyme. " +
                "De plus, elle doit être publique"
            );

        this.resultClass = resultClass;
        return this;
    }

    public List<QueryParameter> getParameters() {
        return parameters;
    }

    public Query<T> setParameter(String name, Object value) {
        parameters.add(new QueryParameter(this, name, value));

        return this;
    }

    public Query<T> setParameter(int position, Object value) {
        parameters.add(new QueryParameter(this, position, value));

        return this;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public Query<T> setFirstResult(int firstResult) {
        Assert.positive(firstResult, "L'argument firstResult ne peut pas être négatif ou nul");

        this.firstResult = firstResult;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public Query<T> setMaxResults(int maxResults) {
        Assert.positiveOrZero(maxResults, "L'argument de maxResults ne peut pas être négatif");

        if (firstResult >= maxResults)
            throw new IllegalArgumentException(
                String.format("L'argument maxResults (=%d) ne peut pas être inférieur ou égale à firstResult (=%d)", maxResults, firstResult)
            );

        this.maxResults = maxResults;
        return this;
    }

    public List<T> getResultsAsList() throws DatabaseException {
        try {
            return SQLExecutor.query(
                session.connection(), sql, (RowMapper<T>) this::processResultSet, firstResult, maxResults, prepareParameters()
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public T getSingleResult() throws DatabaseException, NoResultException, NotSingleResultException {
        try {
            return SQLExecutor.queryForObject(
                session.connection(), sql, this::processResultSet, firstResult, maxResults, prepareParameters()
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Object uniqueColumnResult()
        throws DatabaseException, NoResultException, NonUniqueColumnException, NotSingleResultException {
        try {
            Class<?> resultType = resultClass == null ? Object.class : resultClass;
            return SQLExecutor.queryForUniqueColumn(
                session.connection(), sql, resultType, firstResult, maxResults, prepareParameters()
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public int executeUpdate() throws DatabaseException {
        try {
            return SQLExecutor.update(session.connection(), sql, prepareParameters());
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private Object[] prepareParameters() {
        parameters.sort(Comparator.comparingInt(QueryParameter::getIndex));

        Object[] params = new Object[parameters.size()];
        for (int i = 0; i < params.length; i++)
            params[i] = parameters.get(i).getValue();

        return params;
    }

    @SuppressWarnings("unchecked")
    private T processResultSet(ResultSet resultSet) throws SQLException {
        if (resultClass != null)
            return UtilFunctions.resultSetToObject(resultClass, resultSet);

        int columnCount = resultSet.getMetaData().getColumnCount();

        Object[] objects = new Object[columnCount];
        for (int i = 1; i <= columnCount; i++)
            objects[i - 1] = resultSet.getObject(i);

        return (T) objects;
    }
 }
