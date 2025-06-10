package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.http.exceptions.ForbiddenException;

public class UnauthenticatedUserException extends ForbiddenException {

    public UnauthenticatedUserException() {
        super("Impossible d'accéder à l'utilisateur courant car il n'est pas authentifié");
    }

    public UnauthenticatedUserException(String message) {
        super(message);
    }
}
