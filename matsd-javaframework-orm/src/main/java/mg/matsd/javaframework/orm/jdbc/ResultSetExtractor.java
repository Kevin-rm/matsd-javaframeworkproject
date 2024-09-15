package mg.matsd.javaframework.orm.jdbc;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetExtractor<T> {
    T extractData(ResultSet resultSet);
}
