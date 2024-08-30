package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class NonUniqueColumnException extends BaseException {
    public NonUniqueColumnException(String sql) {
        super(String.format("La requête \"%s\" a renvoyé plusieurs colonnes alors qu'une seule était attendue", sql));
    }
}
