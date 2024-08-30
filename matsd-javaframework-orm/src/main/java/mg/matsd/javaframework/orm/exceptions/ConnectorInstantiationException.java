package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ConnectorInstantiationException extends BaseException {
    private static final String PREFIX = "Erreur lors de l'instanciation du connecteur pour la base de données";

    public ConnectorInstantiationException(Throwable cause) {
        super(PREFIX, cause);
    }
}
