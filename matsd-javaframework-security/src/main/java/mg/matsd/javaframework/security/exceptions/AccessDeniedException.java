package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.servletwrapper.exceptions.ForbiddenException;

import java.util.List;

public class AccessDeniedException extends ForbiddenException {
    private static final String PREFIX = "Accès refusé";
    private final String unreachableResource;
    private List<String> necessaryRoles;

    public AccessDeniedException(String unreachableResource, List<String> necessaryRoles) {
        super(String.format("Vous ne pouvez pas accéder à la ressource \"%s\" " +
            "car vous n'avez pas les rôles nécessaires : %s", unreachableResource, necessaryRoles), PREFIX);

        this.unreachableResource = unreachableResource;
        this.necessaryRoles      = necessaryRoles;
    }

    public AccessDeniedException(String message, String unreachableResource) {
        super(message, PREFIX);
        this.unreachableResource = unreachableResource;
    }

    public String getUnreachableResource() {
        return unreachableResource;
    }

    public List<String> getNecessaryRoles() {
        return necessaryRoles;
    }
}
