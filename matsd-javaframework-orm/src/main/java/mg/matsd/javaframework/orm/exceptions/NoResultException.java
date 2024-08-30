package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class NoResultException extends BaseException {
    public NoResultException(String sql) {
        super(String.format("Aucun résultat retourné par la requête \"%s\"", sql));
    }
}
