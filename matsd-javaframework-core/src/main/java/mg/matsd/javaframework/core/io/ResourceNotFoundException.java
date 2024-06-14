package mg.matsd.javaframework.core.io;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
