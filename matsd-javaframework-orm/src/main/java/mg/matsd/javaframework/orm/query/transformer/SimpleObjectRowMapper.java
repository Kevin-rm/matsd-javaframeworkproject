package mg.matsd.javaframework.orm.query.transformer;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;
import mg.matsd.javaframework.orm.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleObjectRowMapper<T> implements RowMapper<T> {
    @Nullable
    private final Class<T> resultClass;

    public SimpleObjectRowMapper(@Nullable Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet resultSet) throws SQLException {
        Assert.notNull(resultSet, "L'argument resultSet ne peut pas être \"null\"");

        if (resultClass != null)
            return UtilFunctions.resultSetRowToObject(resultClass, resultSet);

        int columnCount = resultSet.getMetaData().getColumnCount();

        Object[] objects = new Object[columnCount];
        for (int i = 1; i <= columnCount; i++)
            objects[i - 1] = resultSet.getObject(i);

        return (T) objects;
    }
}
