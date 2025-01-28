package mg.matsd.javaframework.security.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.exceptions.InvalidCredentialsException;
import mg.matsd.javaframework.security.provider.UserProvider;

public final class AuthenticationManager {
    public static final String DEFAULT_STATEFUL_STORAGE_KEY = "_security_user";

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
        Assert.notBlank(statefulStorageKey, true, "L'argument statefulStorageKey ne peut être vide");

        this.statefulStorageKey = statefulStorageKey;
        return this;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    private AuthenticationManager setUserProvider(UserProvider userProvider) {
        Assert.notNull(userProvider, "L'argument userProvider ne peut pas être \"null\"");

        this.userProvider = userProvider;
        return this;
    }

    public PasswordHasher getPasswordHasher() {
        return passwordHasher;
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

    public void useDefaultStatefulStorageKey() {
        setStatefulStorageKey(DEFAULT_STATEFUL_STORAGE_KEY);
    }

    public boolean attempt(String identifier, String plainPassword) {
        User user = userProvider.loadUserByIdentifier(identifier);

        return user.getIdentifier().equals(identifier) && passwordHasher.verify(plainPassword, user.getPassword());
    }

    public void login(String identifier, String plainPassword) throws InvalidCredentialsException {
        if (!attempt(identifier, plainPassword)) throw new InvalidCredentialsException(identifier, plainPassword);

        currentUser = userProvider.loadUserByIdentifier(identifier);
    }

    public void removeCurrentUser() {
        if (currentUser == null) return;
        currentUser = null;
    }
}
