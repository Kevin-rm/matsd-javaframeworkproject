package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class AccessDeniedException extends BaseException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
