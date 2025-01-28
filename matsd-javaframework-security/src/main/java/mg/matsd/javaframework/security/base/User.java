package mg.matsd.javaframework.security.base;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.List;

public interface User {
    String getIdentifier();

    String getPassword();

    List<UserRole> getRoles();

    default boolean hasRole(@Nullable UserRole role, boolean ignoreCase) {
        if (role == null) return false;

        List<UserRole> userRoles = getRoles();
        if (userRoles == null) return false;

        return userRoles.stream().anyMatch(userRole -> {
            String userRoleValue = userRole.value();
            String roleValue     = role.value();

            return ignoreCase ? userRoleValue.equalsIgnoreCase(roleValue) : userRoleValue.equals(roleValue);
        });
    }

    default boolean hasRole(@Nullable UserRole role) {
        return hasRole(role, false);
    }
}
