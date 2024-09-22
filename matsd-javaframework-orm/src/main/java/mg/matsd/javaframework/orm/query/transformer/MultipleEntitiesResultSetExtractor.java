package mg.matsd.javaframework.orm.query.transformer;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.orm.base.internal.RelationshipPrimaryKeyValue;
import mg.matsd.javaframework.orm.jdbc.ResultSetExtractor;
import mg.matsd.javaframework.orm.mapping.Entity;
import mg.matsd.javaframework.orm.query.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mg.matsd.javaframework.orm.base.internal.UtilFunctions.*;

public class MultipleEntitiesResultSetExtractor<T> implements ResultSetExtractor<T> {
    private final Query<T> query;

    public MultipleEntitiesResultSetExtractor(Query<T> query) {
        Assert.notNull(query, "L'argument query ne peut pas être \"null\"");

        this.query = query;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T extractData(ResultSet resultSet) throws SQLException {
        Assert.notNull(resultSet, "L'argument resultSet ne peut pas être \"null\"");

        Entity entity = query.getSession().getEntity(query.getResultClass());
        String sql    = query.getSql();

        Map<List<Object>, Object> instances = new HashMap<>();
        Map<RelationshipPrimaryKeyValue, Object> toOneInstances = new HashMap<>();

        while (resultSet.next()) {
            List<Object> primaryKeyValue = retrievePrimaryKeyValue(entity, sql, resultSet);

            Object instance = instances.get(primaryKeyValue);
            if (instance == null) {
                instance = hydrateSingleEntity(null, null, entity, sql, resultSet, toOneInstances);
                instances.put(primaryKeyValue, instance);
            }

            fetchEagerToManyRelationships(entity, instance, sql, resultSet, toOneInstances);
        }

        return (T) new ArrayList<>(instances.values());
    }
}
