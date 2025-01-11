package mg.matsd.javaframework.security.base;

import mg.matsd.javaframework.core.utils.Assert;

import java.util.Objects;

public record DefaultUserRoleImpl(String value) implements UserRole {
    public DefaultUserRoleImpl {
        Assert.notBlank(value, false, "La valeur d'une fonction d'un utilisateur ne peut pas être vide ou \"null\"");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        DefaultUserRoleImpl that = (DefaultUserRoleImpl) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        return "DefaultUserRoleImpl{" +
            "value='" + value + '\'' +
            '}';
    }
}
