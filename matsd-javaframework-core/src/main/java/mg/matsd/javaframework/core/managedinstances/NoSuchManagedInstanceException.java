package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class NoSuchManagedInstanceException extends BaseException {
    public NoSuchManagedInstanceException(String message) {
        super(message);
    }
}
