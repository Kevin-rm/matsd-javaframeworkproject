package mg.matsd.javaframework.orm.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet resultSet) throws SQLException;
}
