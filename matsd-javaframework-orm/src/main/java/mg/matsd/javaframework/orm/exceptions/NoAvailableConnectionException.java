package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class NoAvailableConnectionException extends BaseException {
    public NoAvailableConnectionException() {
        super("Le pool de connexions est à court de connexions disponibles");
    }
}
