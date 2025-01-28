package mg.matsd.javaframework.security.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.exceptions.InvalidCredentialsException;
import mg.matsd.javaframework.security.provider.UserProvider;

public final class AuthenticationManager {
    @Nullable
    private String statefulStorageKey;
    private UserProvider userProvider;
    private PasswordHasher passwordHasher;
    @Nullable
    private User currentUser;

    public AuthenticationManager(@Nullable String statefulStorageKey, UserProvider userProvider, PasswordHasher passwordHasher) {
        this.setStatefulStorageKey(statefulStorageKey)
            .setUserProvider(userProvider)
            .setPasswordHasher(passwordHasher);
    }

    public AuthenticationManager(UserProvider userProvider, PasswordHasher passwordHasher) {
        this(null, userProvider, passwordHasher);
    }

    @Nullable
    public String getStatefulStorageKey() {
        return statefulStorageKey;
    }

    private AuthenticationManager setStatefulStorageKey(@Nullable String statefulStorageKey) {
        Assert.notBlank(statefulStorageKey, true, "La clé pour un stockage \"stateful\" ne peut être vide");

        this.statefulStorageKey = statefulStorageKey;
        return this;
    }

    private AuthenticationManager setUserProvider(UserProvider userProvider) {
        Assert.notNull(userProvider, "L'argument userProvider ne peut pas être \"null\"");

        this.userProvider = userProvider;
        return this;
    }

    private AuthenticationManager setPasswordHasher(PasswordHasher passwordHasher) {
        Assert.notNull(passwordHasher, "L'argument passwordHasher ne peut pas être \"null\"");

        this.passwordHasher = passwordHasher;
        return this;
    }

    @Nullable
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(@Nullable User currentUser) {
        this.currentUser = currentUser;
    }

    public boolean attempt(String identifier, String plainPassword) {
        User user = userProvider.loadUserByIdentifier(identifier);

        return user.getIdentifier().equals(identifier) && passwordHasher.verify(plainPassword, user.getPassword());
    }

    public void login(String identifier, String plainPassword) throws InvalidCredentialsException {
        if (!attempt(identifier, plainPassword)) throw new InvalidCredentialsException("");

        currentUser = userProvider.loadUserByIdentifier(identifier);
    }
}
