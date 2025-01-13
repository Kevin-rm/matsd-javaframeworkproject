package mg.matsd.javaframework.security.provider;

import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.UserNotFoundException;

public interface UserProvider {
    User loadUserByIdentifier(String identifier) throws UserNotFoundException;

    User refreshUser(User user) throws UserNotFoundException;
}
