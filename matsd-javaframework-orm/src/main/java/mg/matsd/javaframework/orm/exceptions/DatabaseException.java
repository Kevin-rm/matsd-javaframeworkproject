package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.BaseException;

public class DatabaseException extends BaseException {
    private static final String PREFIX = "Échec d'une opération sur la base de données";

    public DatabaseException(@Nullable String message, @Nullable Throwable cause) {
        super(message, PREFIX, cause);
    }

    public DatabaseException(Throwable cause) {
        super(PREFIX, cause);
    }
}
