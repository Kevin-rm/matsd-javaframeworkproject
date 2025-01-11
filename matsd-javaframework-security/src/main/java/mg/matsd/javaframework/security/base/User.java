package mg.matsd.javaframework.security.base;

import java.util.List;

public interface User {
    String getIdentifier();

    String getPassword();

    List<UserRole> getRoles();
}
