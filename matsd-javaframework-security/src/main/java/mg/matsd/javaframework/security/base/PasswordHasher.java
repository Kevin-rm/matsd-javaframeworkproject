package mg.matsd.javaframework.security.base;

public interface PasswordHasher {
    String hash(String plainPassword);

    boolean verify(String plainPassword, String hashedPassword);
}
