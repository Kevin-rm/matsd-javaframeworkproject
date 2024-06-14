package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.exceptions.BaseException;

class ManagedInstanceCreationException extends BaseException {
    private static final String PREFIX = "Erreur lors de la création d'une \"ManagedInstance\"";

    ManagedInstanceCreationException(String message) {
        super(message, PREFIX);
    }
}
