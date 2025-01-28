package mg.matsd.javaframework.security.base;

import mg.matsd.javaframework.security.provider.UserProvider;

public final class AuthenticationManager {
    public static final String AUTHENTICATION_USER_KEY = "_security_user";

    private UserProvider userProvider;
    private PasswordHasher passwordHasher;

    public boolean attempt(String identifier, String plainPassword) {
        User user = userProvider.loadUserByIdentifier(identifier);

        return user.getIdentifier().equals(identifier) && passwordHasher.verify(plainPassword, user.getPassword());
    }
}
