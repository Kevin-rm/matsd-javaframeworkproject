package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.orm.mapping.Entity;

public class NotSingleResultException extends BaseException {
    public NotSingleResultException(String sql) {
        super(String.format("La requête \"%s\" a retourné plus d'une ligne, en dépit de l'attente d'un seul résultat", sql));
    }

    public NotSingleResultException(String sql, Entity entity) {
        super(String.format("La requête \"%s\" a retourné plusieurs entités de type \"%s\", alors qu'une seule était attendue",
            sql, entity.getClazz()));
    }
}
