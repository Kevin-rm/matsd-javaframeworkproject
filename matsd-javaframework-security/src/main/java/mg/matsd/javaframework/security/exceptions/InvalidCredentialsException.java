package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super("Identifiants invalides");
    }
}
