package mg.itu.prom16.utils;

import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.AuthenticationManager;
import mg.matsd.javaframework.security.base.Security;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.InvalidCredentialsException;

import static mg.itu.prom16.utils.WebFacade.*;

public abstract class AuthFacade {
    @Nullable
    private static AuthenticationManager authenticationManager;

    public static void login(String identifier, String password) throws InvalidCredentialsException {
        AuthenticationManager authenticationManager = statefulWellConfiguredAuthenticationManager();

        authenticationManager.login(identifier, password);
        getCurrentSession().put(authenticationManager.getStatefulStorageKey(), authenticationManager.getCurrentUser());
    }

    public static void logout() {
        AuthenticationManager authenticationManager = statefulWellConfiguredAuthenticationManager();

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
        if (authenticationManager != null) return authenticationManager;

        WebApplicationContainer webApplicationContainer = getWebApplicationContainer();
        authenticationManager = webApplicationContainer.containsManagedInstance(Security.class) ?
            webApplicationContainer.getManagedInstance(Security.class).getAuthenticationManager() : null;

        return authenticationManager;
    }

    private static AuthenticationManager statefulWellConfiguredAuthenticationManager() {
        AuthenticationManager authenticationManager = getAuthenticationManager();
        Assert.state(authenticationManager != null && authenticationManager.getStatefulStorageKey() != null,
            "Le gestionnaire de sécurité et sa clé de stockage de session doivent être configurés " +
                "avant de pouvoir utiliser cette méthode");

        return authenticationManager;
    }
}
