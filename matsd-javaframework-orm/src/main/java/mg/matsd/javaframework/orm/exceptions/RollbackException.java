package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class RollbackException extends BaseException {
    private static final String PREFIX = "Erreur durant une tentative de rollback";

    public RollbackException(Throwable cause) {
        super(PREFIX, cause);
    }
}
