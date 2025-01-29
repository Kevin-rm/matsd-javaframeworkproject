package mg.matsd.javaframework.security.base.implementation;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.PasswordHasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SimpleSHA256PasswordHasher implements PasswordHasher {
    private static final String ALGORITHM = "SHA-256";

    @Override
    public String hash(String plainPassword) {
        Assert.notNull(plainPassword, "Le mot de passe ne peut pas être \"null\"");

        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance(ALGORITHM)
                .digest(plainPassword.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("L'algorithme \"%s\" n'est pas disponible", ALGORITHM), e);
        }
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
        Assert.notNull(plainPassword, "Le mot de passe ne peut pas être \"null\"");
        Assert.notNull(hashedPassword, "Le mot de passe haché ne peut pas être \"null\"");

        return hash(plainPassword).equals(hashedPassword);
    }
}
