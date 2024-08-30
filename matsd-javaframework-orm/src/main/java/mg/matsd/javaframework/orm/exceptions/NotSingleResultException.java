package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class NotSingleResultException extends BaseException {
    public NotSingleResultException(String sql) {
        super(String.format("La requête \"%s\" a retourné plus d'une ligne, en dépit de l'attente d'un seul résultat", sql));
    }
}
