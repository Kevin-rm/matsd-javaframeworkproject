package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class DuplicateUserException extends BaseException {
    private static final String PREFIX = "Utilisateur déjà existant";
    private final String identifier;

    public DuplicateUserException(String identifier) {
        super(String.format("Un utilisateur avec l'identifiant \"%s\" existe déjà dans le provider", identifier), PREFIX);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
