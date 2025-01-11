package mg.matsd.javaframework.security.base;

import mg.matsd.javaframework.core.utils.Assert;

import java.util.Objects;

public class DefaultUserRoleImpl implements UserRole {
    private final String value;

    public DefaultUserRoleImpl(String value) {
        Assert.notBlank(value, false, "La valeur d'une fonction d'un utilisateur ne peut pas être vide ou \"null\"");

        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        DefaultUserRoleImpl that = (DefaultUserRoleImpl) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "DefaultUserRoleImpl{" +
            "value='" + value + '\'' +
            '}';
    }
}
