package mg.matsd.javaframework.security.exceptions;

public abstract class AbstractForbiddenException extends HttpStatusException {

    public AbstractForbiddenException(String message) {
        super(message);
    }

    @Override
    protected int defineStatusCode() {
        return 403;
    }
}
