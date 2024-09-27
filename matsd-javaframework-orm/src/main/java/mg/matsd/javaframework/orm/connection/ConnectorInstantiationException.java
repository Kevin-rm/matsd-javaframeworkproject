package mg.matsd.javaframework.orm.connection;

import mg.matsd.javaframework.core.exceptions.BaseException;

class ConnectorInstantiationException extends BaseException {
    private static final String PREFIX = "Erreur lors de l'instanciation du connecteur pour la base de données";

    ConnectorInstantiationException(Throwable cause) {
        super(PREFIX, cause);
    }
}
