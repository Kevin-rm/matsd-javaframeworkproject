package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.exceptions.BaseException;

class ManagedInstanceDefinitionException extends BaseException {
    private static final String PREFIX = "Erreur lors de la définition de \"ManagedInstance\"";

    ManagedInstanceDefinitionException(String message) {
        super(message, PREFIX);
    }

    ManagedInstanceDefinitionException(Throwable cause) {
        super(PREFIX, cause);
    }
}
