package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class BadQueryException extends BaseException {
    private static final String PREFIX = "Requête SQL non valide";

    public BadQueryException(String message) {
        super(message, PREFIX);
    }
}
