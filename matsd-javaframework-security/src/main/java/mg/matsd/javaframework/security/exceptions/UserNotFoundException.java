package mg.matsd.javaframework.security.exceptions;

public class UserNotFoundException extends RuntimeException {
    private static final String PREFIX = "Utilisateur non trouvé";
    private final String identifier;

    public UserNotFoundException(String identifier) {
        super("Aucun utilisateur trouvé avec l'identifiant: " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
