package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.exceptions.BaseException;

class ConfigurationException extends BaseException {
    private static final String PREFIX  = "Erreur lors de la configuration de la base de données";

    ConfigurationException(String message) {
        super(message, PREFIX);
    }

    ConfigurationException(String message, Throwable cause) {
        super(message, PREFIX, cause);
    }
}
