package mg.matsd.javaframework.validation.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ConstraintDefinitionException extends BaseException {
    private static final String PREFIX = "Erreur lors de la définition d'une contrainte";

    public ConstraintDefinitionException(String message) {
        super(message, PREFIX);
    }
}
