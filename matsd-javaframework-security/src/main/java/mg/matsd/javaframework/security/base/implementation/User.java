package mg.matsd.javaframework.security.base.implementation;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.ArrayUtils;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.PasswordHasher;
import mg.matsd.javaframework.security.base.UserRole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User implements mg.matsd.javaframework.security.base.User {
    private String username;
    private String password;
    private List<UserRole> roles;
    @Nullable
    private PasswordHasher passwordHasher;

    public User(String username, String password, String... roles) {
        this.setUsername(username)
            .setPassword(password)
            .setRoles(roles);
    }

    private User setUsername(String username) {
        Assert.notBlank(username, false, "Le nom d'utilisateur ne peut pas être vide ou \"null\"");

        this.username = username;
        return this;
    }

    public User setPassword(String password) {
        Assert.notNull(password, "Le mot de passe ne peut pas être \"null\"");

        this.password = passwordHasher == null ? password : passwordHasher.hash(password);
        return this;
    }

    public User setRoles(@Nullable String... roles) {
        if (ArrayUtils.isEmpty(roles)) return this;
        Assert.noNullElements(roles, "Chaque rôle du tableau ne peut pas être \"null\"");

        this.roles = new ArrayList<>();
        Arrays.stream(roles).forEachOrdered(role -> this.roles.add(new SimpleUserRole(role)));

        return this;
    }

    @Nullable
    public PasswordHasher getPasswordHasher() {
        return passwordHasher;
    }

    public User setPasswordHasher(@Nullable PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
        return this;
    }

    @Override
    public String getIdentifier() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public List<UserRole> getRoles() {
        return roles;
    }
}
