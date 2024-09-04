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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RawQuery<T> {
    private final Session session;
    private final String originalSql;
    private String sql;
    @Nullable
    private Class<T> resultClass;
    private int firstResult = -1;
    private int maxResults  = -1;
    private final List<QueryParameter> parameters;

    public RawQuery(Session session, String sql, @Nullable Class<T> resultClass) {
        Assert.notNull(session, "La session ne peut pas être \"null\"");

        this.setSql(sql)
            .setResultClass(resultClass);

        this.session = session;
        originalSql  = this.sql;
        parameters   = new ArrayList<>();
    }

    public RawQuery(Session session, String sql) {
        this(session, sql, null);
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public String getSql() {
        return sql;
    }

    RawQuery<T> setSql(String sql) {
        Assert.notBlank(sql, false, "La requête SQL ne peut pas être vide ou \"null\"");

        this.sql = sql.strip();
        return this;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    RawQuery<T> setResultClass(@Nullable Class<T> resultClass) {
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

    public RawQuery<T> setParameter(String name, Object value) {
        parameters.add(new QueryParameter(this, name, value));

        return this;
    }

    public RawQuery<T> setParameter(int position, Object value) {
        parameters.add(new QueryParameter(this, position, value));

        return this;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public RawQuery<T> setFirstResult(int firstResult) {
        Assert.positive(firstResult, "L'argument firstResult ne peut pas être négatif ou nul");

        this.firstResult = firstResult;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public RawQuery<T> setMaxResults(int maxResults) {
        Assert.positiveOrZero(maxResults, "L'argument de maxResults ne peut pas être négatif");

        if (firstResult >= maxResults)
            throw new IllegalArgumentException(
                String.format("L'argument maxResults (=%d) ne peut pas être inférieur ou égale à firstResult (=%d)", maxResults, firstResult)
            );

        this.maxResults = maxResults;
        return this;
    }

    public List<T> getResultsAsList() {
        try {
            return SQLExecutor.query(
                session.connection(), sql, this::processResultSet, firstResult, maxResults, prepareParameters()
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public T getSingleResult() {
        return null;
    }

    public Object uniqueColumnResult()
        throws NonUniqueColumnException, NoResultException, NotSingleResultException {
        try {
            Class<?> resultType = resultClass == null ? Object.class : resultClass;
            return SQLExecutor.queryForUniqueColumn(
                session.connection(), sql, resultType, firstResult, maxResults, prepareParameters()
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public int executeUpdate() {
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
