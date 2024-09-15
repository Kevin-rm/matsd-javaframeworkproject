package mg.matsd.javaframework.orm.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetExtractor<T> {
    T extractData(ResultSet resultSet) throws SQLException;
}
