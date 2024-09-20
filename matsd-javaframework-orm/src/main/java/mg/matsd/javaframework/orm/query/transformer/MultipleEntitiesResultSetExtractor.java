package mg.matsd.javaframework.orm.query.transformer;

import mg.matsd.javaframework.core.utils.Assert;
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

        Map<List<Object>, Object> instances = new HashMap<>();

        Entity entity = query.getSession().getEntity(query.getResultClass());
        while (resultSet.next()) {
            List<Object> primaryKeyValue = retrievePrimaryKeyValue(entity, query.getSql(), resultSet);

            Object instance = instances.get(primaryKeyValue);
            if (instance == null) {
                instance = hydrateSingleEntity(entity, resultSet);
                instances.put(primaryKeyValue, instance);
            }

            fecthEagerToManyRelationships(entity, instance, resultSet);
        }

        return (T) new ArrayList<>(instances.values());
    }
}
