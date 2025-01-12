package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class AccessDeniedException extends BaseException {
    private static final String PREFIX = "Accès refusé";

    public AccessDeniedException(String message) {
        super(message);
    }
}
