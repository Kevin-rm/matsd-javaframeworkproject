package mg.matsd.javaframework.core.io;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.core.utils.StringUtils;

public class ResourceNotFoundException extends BaseException {
    private final String resourceName;

    public ResourceNotFoundException(@Nullable final String message, final String resourceName) {
        super(StringUtils.isNullOrBlank(message) ?
            String.format("La ressource \"%s\" est introuvable", resourceName) : message);
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String resourceName) {
        this(null, resourceName);
    }

    public String getResourceName() {
        return resourceName;
    }
}
