package mg.matsd.javaframework.validation.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ValidationException extends BaseException {
    private static final String PREFIX = "Erreur de validation de données";

    public ValidationException(String message) {
        super(PREFIX, message);
    }
}
