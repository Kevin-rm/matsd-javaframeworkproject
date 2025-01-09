package mg.matsd.javaframework.security.base;

public interface User {
    String[] getRoles();

    String getIdentifier();

    String getPassword();
}
