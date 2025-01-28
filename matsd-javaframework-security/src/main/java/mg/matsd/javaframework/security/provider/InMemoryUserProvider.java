package mg.matsd.javaframework.security.provider;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.UserNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class InMemoryUserProvider implements UserProvider {
    private final Map<String, User> usersMap;

    public InMemoryUserProvider(User... users) {
        Assert.notEmpty(users, "Le tableau d'utilisateurs ne peut être vide ou \"null\"");
        Assert.noNullElements(users, "Chaque utilisateur du tableau ne peut être \"null\"");

        usersMap = new HashMap<>();
        Arrays.stream(users).forEachOrdered(user -> usersMap.put(user.getIdentifier(), user));
    }

    @Override
    public User loadUserByIdentifier(String identifier) throws UserNotFoundException {
        Assert.notBlank(identifier, false, "L'identifiant de l'utilisateur ne peut être vide ou \"null\"");

        if (!usersMap.containsKey(identifier)) throw new UserNotFoundException(identifier);
        return usersMap.get(identifier);
    }
}
