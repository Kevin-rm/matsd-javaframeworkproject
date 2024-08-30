package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class MappingException extends BaseException {
    private static final String PREFIX = "Erreur de mapping rencontré";

    public MappingException(String message) {
        super(message, PREFIX);
    }
}
