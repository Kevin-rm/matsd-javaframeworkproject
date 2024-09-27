package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.orm.mapping.Entity;

public class NoResultException extends BaseException {
    public NoResultException(String sql) {
        super(String.format("Aucun résultat retourné par la requête \"%s\"", sql));
    }

    public NoResultException(String sql, Entity entity) {
        super(String.format("Aucune entité de type \"%s\" n'a été retournée par la requête \"%s\"",
            entity.getClazz(), sql));
    }
}
