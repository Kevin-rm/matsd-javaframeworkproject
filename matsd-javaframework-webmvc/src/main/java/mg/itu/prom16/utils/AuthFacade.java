package mg.itu.prom16.utils;

import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.AuthenticationManager;
import mg.matsd.javaframework.security.base.Security;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.InvalidCredentialsException;

import java.util.function.Function;

import static mg.itu.prom16.utils.WebFacade.*;

public abstract class AuthFacade {
    @Nullable
    private static AuthenticationManager authenticationManager;

    public static void login(String identifier, String password) throws InvalidCredentialsException {
        AuthenticationManager authenticationManager = statefulWellConfiguredAuthenticationManager();

        authenticationManager.login(identifier, password);
        currentSession().set(authenticationManager.getStatefulStorageKey(), authenticationManager.getCurrentUser());
    }

    public static void logout() {
        AuthenticationManager authenticationManager = statefulWellConfiguredAuthenticationManager();

        authenticationManager.removeCurrentUser();
        currentSession().remove(authenticationManager.getStatefulStorageKey());
    }

    public static boolean isUserAuthenticated() {
        return currentUser() != null;
    }

    @Nullable
    public static User currentUser() {
        return withAuthenticationManager(AuthenticationManager::getCurrentUser);
    }

    @Nullable
    public static <U extends User> U currentUser(final Class<U> expectedType) {
        return withAuthenticationManager(am -> am.getCurrentUser(expectedType));
    }

    public static User requireCurrentUser() {
        return withAuthenticationManager(AuthenticationManager::requireCurrentUser, true);
    }

    public static <U extends User> U requireCurrentUser(final Class<U> expectedType) {
        return withAuthenticationManager(am -> am.requireCurrentUser(expectedType), true);
    }

    @Nullable
    public static AuthenticationManager getAuthenticationManager() {
        if (authenticationManager != null) return authenticationManager;

        WebApplicationContainer webApplicationContainer = webApplicationContainer();
        authenticationManager = webApplicationContainer.containsManagedInstance(Security.class) ?
            webApplicationContainer.getManagedInstance(Security.class).getAuthenticationManager() : null;

        return authenticationManager;
    }

    @Nullable
    public static <T> T withAuthenticationManager(
        final Function<AuthenticationManager, T> function, @Nullable final Boolean required
    ) {
        AuthenticationManager authenticationManager = getAuthenticationManager();
        if (authenticationManager == null) {
            if (!required) return null;
            throw new IllegalStateException("Le gestionnaire d'authentification ne peut pas être \"null\" " +
                "pour pouvoir utiliser cette méthode");
        }

        return function.apply(authenticationManager);
    }

    @Nullable
    public static <T> T withAuthenticationManager(final Function<AuthenticationManager, T> function) {
        return withAuthenticationManager(function, false);
    }

    private static AuthenticationManager statefulWellConfiguredAuthenticationManager() {
        AuthenticationManager authenticationManager = getAuthenticationManager();
        Assert.state(authenticationManager != null && authenticationManager.getStatefulStorageKey() != null,
            "Le gestionnaire d'authentification et sa clé de stockage de session doivent être configurés " +
                "avant de pouvoir utiliser cette méthode");

        return authenticationManager;
    }
}
