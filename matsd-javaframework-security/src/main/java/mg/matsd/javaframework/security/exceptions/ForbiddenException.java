package mg.matsd.javaframework.security.exceptions;

public class ForbiddenException extends HttpStatusException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, String prefix) {
        super(message, prefix);
    }

    @Override
    protected int defineStatusCode() {
        return 403;
    }
}
