package mg.matsd.javaframework.orm.connection;

import mg.matsd.javaframework.core.exceptions.BaseException;

class NoAvailableConnectionException extends BaseException {
    NoAvailableConnectionException() {
        super("Le pool de connexions est à court de connexions disponibles");
    }
}
