package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.security.base.UserRole;

import java.util.List;

public class AccessDeniedException extends BaseException {
    private static final String PREFIX = "Accès refusé";
    private final String unreachableResource;
    private final List<UserRole> necessaryUserRoles;

    public AccessDeniedException(String unreachableResource, List<UserRole> necessaryUserRoles) {
        super(String.format("Vous ne pouvez pas accéder à la ressource \"%s\"" +
            "car vous n'avez pas les fonctions nécessaires : %s", unreachableResource, necessaryUserRoles), PREFIX);
        this.unreachableResource = unreachableResource;
        this.necessaryUserRoles  = necessaryUserRoles;
    }

    public String getUnreachableResource() {
        return unreachableResource;
    }

    public List<UserRole> getNecessaryUserRoles() {
        return necessaryUserRoles;
    }
}
