package mg.matsd.javaframework.validation.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ValidationProcessException extends BaseException {
    private static final String PREFIX = "Erreur rencontrée durant un processus de validation";

    public ValidationProcessException(Throwable cause) {
        super(PREFIX, cause);
    }
}
