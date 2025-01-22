package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class UserNotFoundException extends BaseException {
    private static final String PREFIX = "Utilisateur non trouvé";
    private final String identifier;

    public UserNotFoundException(String identifier) {
        super("Aucun utilisateur trouvé avec l'identifiant : " + identifier, PREFIX);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
