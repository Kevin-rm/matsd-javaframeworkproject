package mg.matsd.javaframework.security.provider;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.DuplicateUserException;
import mg.matsd.javaframework.security.exceptions.UserNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class InMemoryUserProvider implements UserProvider {
    private final Map<String, User> usersMap;

    public InMemoryUserProvider() {
        usersMap = new HashMap<>();
    }

    public InMemoryUserProvider(User... users) {
        Assert.notEmpty(users, "Le tableau d'utilisateurs ne peut être vide ou \"null\"");

        usersMap = new HashMap<>();
        Arrays.stream(users).forEachOrdered(this::addUser);
    }

    @Override
    public void addUser(User user) throws DuplicateUserException {
        Assert.notNull(user, "L'utilisateur ne peut pas être \"null\"");
        final String identifier = user.getIdentifier();
        Assert.notBlank(identifier, false, "L'identifiant de l'utilisateur ne peut être vide ou \"null\"");

        if (usersMap.containsKey(identifier)) throw new DuplicateUserException(identifier);
        usersMap.put(identifier, user);
    }

    @Override
    public User loadUserByIdentifier(String identifier) throws UserNotFoundException {
        Assert.notBlank(identifier, false, "L'identifiant de l'utilisateur ne peut être vide ou \"null\"");

        if (!usersMap.containsKey(identifier)) throw new UserNotFoundException(identifier);
        return usersMap.get(identifier);
    }
}
