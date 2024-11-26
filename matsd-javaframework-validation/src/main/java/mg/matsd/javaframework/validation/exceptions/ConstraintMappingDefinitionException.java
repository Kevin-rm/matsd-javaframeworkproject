package mg.matsd.javaframework.validation.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ConstraintMappingDefinitionException extends BaseException {
    private static final String PREFIX = "Erreur lors de la définition d'un contraintMapping";

    public ConstraintMappingDefinitionException(String message) {
        super(message, PREFIX);
    }
}
