package mg.matsd.javaframework.core.io;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.core.utils.StringUtils;

public class ResourceNotFoundException extends BaseException {
    private final String resourceName;

    public ResourceNotFoundException(@Nullable String message, String resourceName) {
        super(StringUtils.isNullOrBlank(message) ? "Ressource introuvable" : message);
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String resourceName) {
        this(null, resourceName);
    }

    public String getResourceName() {
        return resourceName;
    }
}
