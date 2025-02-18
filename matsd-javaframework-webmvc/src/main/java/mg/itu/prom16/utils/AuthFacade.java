package mg.itu.prom16.utils;

import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.AuthenticationManager;
import mg.matsd.javaframework.security.base.Security;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.InvalidCredentialsException;

import static mg.itu.prom16.utils.WebFacade.*;

public class AuthFacade {
    private AuthFacade() { }

    public static void login(String identifier, String password) throws InvalidCredentialsException {
        AuthenticationManager authenticationManager = getAuthenticationManager();
        Assert.state(authenticationManager != null, "Le gestionnaire de sécurité doit être configuré " +
            "avant de pouvoir utiliser la méthode \"login\"");

        authenticationManager.login(identifier, password);
        getCurrentSession().put(authenticationManager.getStatefulStorageKey(), authenticationManager.getCurrentUser());
    }

    public static void logout() {
        AuthenticationManager authenticationManager = getAuthenticationManager();
        Assert.state(authenticationManager != null, "Le gestionnaire de sécurité doit être configuré " +
            "avant de pouvoir utiliser la méthode \"logout\"");

        authenticationManager.removeCurrentUser();
        getCurrentSession().remove(authenticationManager.getStatefulStorageKey());
    }

    public static boolean isUserConnected() {
        return getCurrentUser() != null;
    }

    @Nullable
    public static User getCurrentUser() {
        AuthenticationManager authenticationManager = getAuthenticationManager();
        return authenticationManager == null ? null : authenticationManager.getCurrentUser();
    }

    @Nullable
    public static AuthenticationManager getAuthenticationManager() {
        WebApplicationContainer webApplicationContainer = getWebApplicationContainer();

        return webApplicationContainer.containsManagedInstance(Security.class) ?
            webApplicationContainer.getManagedInstance(Security.class).getAuthenticationManager() : null;
    }
}
