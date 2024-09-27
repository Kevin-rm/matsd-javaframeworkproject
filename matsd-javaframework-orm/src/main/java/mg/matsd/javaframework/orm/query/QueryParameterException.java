package mg.matsd.javaframework.orm.query;

import mg.matsd.javaframework.core.exceptions.BaseException;

class QueryParameterException extends BaseException {
    private static final String PREFIX = "Erreur au niveau d'un paramètre de requête";

    QueryParameterException(String message) {
        super(message, PREFIX);
    }
}
