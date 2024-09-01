package mg.matsd.javaframework.orm.connection;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class NoAvailableConnectionException extends BaseException {
    public NoAvailableConnectionException() {
        super("Le pool de connexions est à court de connexions disponibles");
    }
}
