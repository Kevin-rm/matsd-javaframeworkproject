package mg.matsd.javaframework.security.provider;

import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.UserNotFoundException;

public class InMemoryUserProvider implements UserProvider {

    @Override
    public User loadUserByIdentifier(String identifier) throws UserNotFoundException {
        return null;
    }

    @Override
    public User refreshUser(User user) throws UserNotFoundException {
        return null;
    }
}
