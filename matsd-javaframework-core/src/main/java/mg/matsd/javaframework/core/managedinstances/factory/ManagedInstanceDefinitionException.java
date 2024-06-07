package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ManagedInstanceDefinitionException extends BaseException {
    private static final String PREFIX = "Erreur lors de la définition de \"ManagedInstance\"";

    public ManagedInstanceDefinitionException(String message) {
        super(message, PREFIX);
    }

    public ManagedInstanceDefinitionException(Throwable cause) {
        super(PREFIX, cause);
    }
}
