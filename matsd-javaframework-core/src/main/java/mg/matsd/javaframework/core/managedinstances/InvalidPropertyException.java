package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.exceptions.BaseException;

class InvalidPropertyException extends BaseException {
    InvalidPropertyException(String message) {
        super(message);
    }
}
