package mg.matsd.javaframework.security.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.List;

public interface User {
    String getIdentifier();

    String getPassword();

    List<? extends UserRole> getRoles();

    default boolean hasRole(@Nullable String roleValue, boolean ignoreCase) {
        if (roleValue == null || StringUtils.isBlank(roleValue)) return false;

        List<? extends UserRole> userRoles = getRoles();
        if (userRoles == null) return false;

        return userRoles.stream().anyMatch(userRole -> {
            String userRoleValue = userRole.value();
            return ignoreCase ? userRoleValue.equalsIgnoreCase(roleValue) : userRoleValue.equals(roleValue);
        });
    }

    default boolean hasRole(@Nullable String roleValue) {
        return hasRole(roleValue, false);
    }
}
