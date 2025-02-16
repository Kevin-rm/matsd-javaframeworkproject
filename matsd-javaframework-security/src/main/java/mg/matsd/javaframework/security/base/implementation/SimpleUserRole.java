package mg.matsd.javaframework.security.base.implementation;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.base.UserRole;

import java.util.Objects;

public class SimpleUserRole implements UserRole {
    protected String value;

    public SimpleUserRole() { }

    public SimpleUserRole(String value) {
        setValue(value);
    }

    @Override
    public String value() {
        return value;
    }

    public void setValue(String value) {
        Assert.notBlank(value, false, "La valeur d'une fonction d'un utilisateur ne peut pas être vide ou \"null\"");
        this.value = value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        SimpleUserRole that = (SimpleUserRole) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "SimpleUserRoleImpl{" +
            "value='" + value + '\'' +
            '}';
    }
}
