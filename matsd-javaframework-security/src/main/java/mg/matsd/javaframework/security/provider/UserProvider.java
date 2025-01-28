package mg.matsd.javaframework.security.provider;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.UserNotFoundException;

public interface UserProvider {
    User loadUserByIdentifier(String identifier) throws UserNotFoundException;

    default User refreshUser(User user) throws UserNotFoundException {
        Assert.notNull(user, "L'utilisateur ne peut pas être \"null\"");

        return loadUserByIdentifier(user.getIdentifier());
    }
}
