package mg.matsd.javaframework.orm.query;

import com.sun.jdi.InternalException;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.orm.base.Session;
import mg.matsd.javaframework.orm.base.internal.SQLExecutor;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;
import mg.matsd.javaframework.orm.exceptions.*;
import mg.matsd.javaframework.orm.jdbc.ResultSetExtractor;
import mg.matsd.javaframework.orm.jdbc.RowMapper;
import mg.matsd.javaframework.orm.mapping.Entity;
import mg.matsd.javaframework.orm.mapping.FetchType;
import mg.matsd.javaframework.orm.mapping.Relationship;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class Query<T> {
    private final Session session;
    private final String originalSql;
    private String sql;
    @Nullable
    private Class<T> resultClass;
    @Nullable
    private RowMapper<T> rowMapper;
    @Nullable
    private ResultSetExtractor<T> resultSetExtractor;
    private int firstResult = -1;
    private int maxResults  = -1;
    private final List<QueryParameter> parameters;

    public Query(Session session, String sql, @Nullable Class<T> resultClass) {
        Assert.notNull(session, "La session ne peut pas être \"null\"");

        this.session = session;
        this.setSql(sql)
            .setResultClass(resultClass);

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
        if (session.isEntity(this.resultClass))
             resultSetExtractor = new SingleEntityResultSetExtractor();
        else rowMapper          = new SimpleObjectRowMapper();

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

    @SuppressWarnings("unchecked")
    public List<T> getResultsAsList() throws DatabaseException {
        try {
            Connection connection = session.connection();
            Object[] parameters   = prepareParameters();

            if (session.isEntity(resultClass))
                return (List<T>) SQLExecutor.query(connection, sql, new MultipleEntitiesResultSetExtractor(), firstResult, maxResults, parameters);

            return SQLExecutor.query(connection, sql, rowMapper, firstResult, maxResults, parameters);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public T getSingleResult() throws DatabaseException, NoResultException, NotSingleResultException {
        try {
            Connection connection = session.connection();
            Object[] parameters   = prepareParameters();

            if (session.isEntity(resultClass))
                return SQLExecutor.query(connection, sql, new SingleEntityResultSetExtractor(), firstResult, maxResults, parameters);

            return SQLExecutor.queryForObject(connection, sql, rowMapper, firstResult, maxResults, parameters);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Object uniqueColumnResult()
        throws DatabaseException, NoResultException, NonUniqueColumnException, NotSingleResultException {
        try {
            Class<?> resultType = resultClass == null ? Object.class : resultClass;
            return SQLExecutor.queryForUniqueColumn(session.connection(), sql, resultType, firstResult, maxResults, prepareParameters());
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

    private class SimpleObjectRowMapper implements RowMapper<T> {
        @Override
        @SuppressWarnings("unchecked")
        public T mapRow(ResultSet resultSet) throws SQLException {
            if (resultClass != null)
                return UtilFunctions.resultSetRowToObject(resultClass, resultSet);

            int columnCount = resultSet.getMetaData().getColumnCount();

            Object[] objects = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++)
                objects[i - 1] = resultSet.getObject(i);

            return (T) objects;
        }
    }

    private class MultipleEntitiesResultSetExtractor implements ResultSetExtractor<T> {
        @Override
        @SuppressWarnings("unchecked")
        public T extractData(ResultSet resultSet) throws SQLException {
            Map<Object, Object> entityInstances = new HashMap<>();

            Entity entity = session.getEntity(resultClass);
            String pkColumnName = entity.getPrimaryKey().get(0).getName();
            while (resultSet.next()) {
                Object pk = getPrimaryKeyValue(pkColumnName, resultSet);

                Object instance = entityInstances.get(pk);
                if (instance == null) {
                    instance = hydrateSingleEntity(entity, resultSet);
                    entityInstances.put(pk, instance);
                }

                loadEagerToManyRelationships(entity, instance, resultSet);
            }

            return (T) new ArrayList<>(entityInstances.values());
        }
    }

    private class SingleEntityResultSetExtractor implements ResultSetExtractor<T> {
        @Override
        @SuppressWarnings("unchecked")
        public T extractData(ResultSet resultSet) throws SQLException {
            Entity entity = session.getEntity(resultClass);

            Object instance = null;
            while (resultSet.next()) {
                if (instance == null)
                    instance = hydrateSingleEntity(entity, resultSet);

                loadEagerToManyRelationships(entity, instance, resultSet);
            }
            if (instance == null) throw new NoResultException(sql);

            return (T) instance;
        }
    }

    private Object getPrimaryKeyValue(String primaryKeyColumnName, ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
            if (resultSetMetaData.getColumnName(i).equals(primaryKeyColumnName))
                return resultSet.getObject(i);

        throw new BadQueryException("");
    }

    private static Object hydrateSingleEntity(Entity entity, ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        return hydrateSingleEntity(
            null, null, entity, resultSet, resultSetMetaData, new int[]{1}, resultSetMetaData.getColumnCount(), new HashMap<>());
    }

    private static Object hydrateSingleEntity(
        @Nullable Object instance,
        @Nullable Entity previous,
        Entity current,
        ResultSet resultSet,
        ResultSetMetaData resultSetMetaData,
        int[] index,
        int   columnCount,
        Map<String, Relationship> visitedRelationships
    ) throws SQLException {
        for (; index[0] <= columnCount; index[0]++) {
            String columnName = resultSetMetaData.getColumnName(index[0]);
            String tableName  = resultSetMetaData.getTableName(index[0]);

            if (current.getTableName().equals(tableName) && current.hasColumn(columnName)) {
                if (instance == null) instance = UtilFunctions.instantiate(current.getClazz());

                UtilFunctions.setFieldValue(instance, current.getColumn(columnName).getField(), resultSet, index[0], null);
            } else {
                if (previous != null && previous.hasRelationship(tableName)) {
                    index[0]--;
                    return instance;
                }

                Relationship relationship = getRelationshipByTableName(current, tableName, visitedRelationships);
                if (relationship == null || relationship.getFetchType() != FetchType.EAGER || relationship.isToMany()) continue;

                Entity targetEntity = relationship.getTargetEntity();
                if (previous == targetEntity) {
                    index[0] --;
                    return instance;
                }

                Field relationshipField = relationship.getField();
                relationshipField.setAccessible(true);
                try {
                    if (instance == null) throw new BadQueryException(String.format("Aucune colonne de l'entité \"%s\" n'a été trouvée dans la requête, " +
                        "pourtant des colonnes de relation avec l'entité \"%s\" y sont présentes", current.getClazz(), targetEntity.getClazz()));

                    Object fieldValue = relationshipField.get(instance);
                    fieldValue = hydrateSingleEntity(fieldValue, current, targetEntity, resultSet, resultSetMetaData, index, columnCount, visitedRelationships);

                    relationshipField.set(instance, fieldValue);
                } catch (IllegalAccessException ignored) { }

                visitedRelationships.put(tableName, relationship);
            }
        }

        return instance;
    }

    @SuppressWarnings("all")
    private static void loadEagerToManyRelationships(Entity entity, Object instance, ResultSet resultSet) throws SQLException {
        for (Relationship relationship : entity.getToManyRelationships()) {;
            Field relationshipField = relationship.getField();
            relationshipField.setAccessible(true);

            try {
                Collection collection = (Collection) relationshipField.get(instance);
                if (collection == null) {
                    collection = new ArrayList<>();
                    relationshipField.set(instance, collection);
                }

                collection.add(hydrateSingleEntity(relationship.getTargetEntity(), resultSet));
            } catch (IllegalAccessException e) {
                throw new InternalException();
            }
        }
    }

    private static Relationship getRelationshipByTableName(Entity current, String tableName, Map<String, Relationship> visitedRelationships) {
        if (visitedRelationships.containsKey(tableName)) return visitedRelationships.get(tableName);

        return current.getRelationships().stream()
            .filter(relationship -> relationship.getTargetEntity().getTableName().equals(tableName))
            .findFirst()
            .orElse(null);
    }
}
