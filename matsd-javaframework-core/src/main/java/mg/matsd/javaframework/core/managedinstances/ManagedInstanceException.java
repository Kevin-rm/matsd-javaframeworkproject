package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ManagedInstanceException extends BaseException {
    public ManagedInstanceException(String message) {
        super(message);
    }

    public ManagedInstanceException(Throwable cause) {
        super(cause);
    }
}
