package mg.itu.prom16.utils;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.security.base.AuthenticationManager;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.InvalidCredentialsException;

import static mg.itu.prom16.utils.WebFacade.*;

public final class AuthFacade {
    private AuthFacade() { }

    public static void login(String identifier, String password) throws InvalidCredentialsException {
        AuthenticationManager authenticationManager = getAuthenticationManager();

        authenticationManager.login(identifier, password);
        getCurrentSession().put(authenticationManager.getStatefulStorageKey(), authenticationManager.getCurrentUser());
    }

    public static void logout() {
        AuthenticationManager authenticationManager = getAuthenticationManager();

        authenticationManager.removeCurrentUser();
        getCurrentSession().remove(authenticationManager.getStatefulStorageKey());
    }

    public static boolean isUserConnected() {
        return getCurrentUser() != null;
    }

    @Nullable
    public static User getCurrentUser() {
        return (User) getCurrentSession().get(getAuthenticationManager().getStatefulStorageKey());
    }

    public static AuthenticationManager getAuthenticationManager() {
        return (AuthenticationManager) getWebApplicationContainer().getManagedInstance(AuthenticationManager.class);
    }
}
