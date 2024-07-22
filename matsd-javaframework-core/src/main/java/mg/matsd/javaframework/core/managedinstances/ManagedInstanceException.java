package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.exceptions.BaseException;

class ManagedInstanceException extends BaseException {
    ManagedInstanceException(String message) {
        super(message);
    }

    ManagedInstanceException(Throwable cause) {
        super(cause);
    }
}
