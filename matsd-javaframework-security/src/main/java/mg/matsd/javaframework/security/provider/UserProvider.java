package mg.matsd.javaframework.security.provider;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.DuplicateUserException;
import mg.matsd.javaframework.security.exceptions.UserNotFoundException;

public interface UserProvider {
    void addUser(User user) throws DuplicateUserException;

    User loadUserByIdentifier(String identifier) throws UserNotFoundException;

    default User refreshUser(User user) throws UserNotFoundException {
        Assert.notNull(user, "L'utilisateur ne peut pas être \"null\"");

        return loadUserByIdentifier(user.getIdentifier());
    }
}
