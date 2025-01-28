package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class InvalidCredentialsException extends BaseException {
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
