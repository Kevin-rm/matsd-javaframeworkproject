package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.http.exceptions.ForbiddenException;

public class InvalidCredentialsException extends ForbiddenException {
    private final String identifier;
    private final String plainPassword;

    public InvalidCredentialsException(final String identifier, final String plainPassword) {
        super("Identifiants invalides");

        this.identifier    = identifier;
        this.plainPassword = plainPassword;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPlainPassword() {
        return plainPassword;
    }
}
