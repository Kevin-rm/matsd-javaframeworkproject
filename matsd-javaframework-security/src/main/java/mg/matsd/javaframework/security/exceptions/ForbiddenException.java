package mg.matsd.javaframework.security.exceptions;

public class ForbiddenException extends HttpStatusException {

    public ForbiddenException(String message) {
        super(message);
    }

    @Override
    protected int defineStatusCode() {
        return 403;
    }
}
