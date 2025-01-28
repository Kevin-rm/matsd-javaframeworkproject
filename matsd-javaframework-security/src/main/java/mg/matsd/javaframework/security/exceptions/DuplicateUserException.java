package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class DuplicateUserException extends BaseException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
