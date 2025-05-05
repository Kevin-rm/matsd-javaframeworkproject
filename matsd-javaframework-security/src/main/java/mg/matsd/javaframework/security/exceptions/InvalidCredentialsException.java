package mg.matsd.javaframework.security.exceptions;

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
