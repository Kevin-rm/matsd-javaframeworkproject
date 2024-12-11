package mg.matsd.javaframework.orm.jdbc;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class JdbcException extends BaseException {
    private static final String PREFIX = "Erreur sur une opération JDBC";

    JdbcException(Throwable cause) {
        super(PREFIX, cause);
    }
}
