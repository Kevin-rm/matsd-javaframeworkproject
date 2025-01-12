package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.security.base.UserRole;

import java.util.List;

public class AccessDeniedException extends BaseException {
    private static final String PREFIX = "Accès refusé";
    private final List<UserRole> userRoles;

    public AccessDeniedException(List<UserRole> userRoles) {
        super("Vous ne pouvez pas accéder à cette ressource car vous n'avez pas les fonctions nécessaires : " + userRoles, PREFIX);
        this.userRoles = userRoles;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }
}
