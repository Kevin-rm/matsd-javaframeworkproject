package mg.matsd.javaframework.security.exceptions;

public class NotFoundHttpException extends HttpStatusException {
    private static final String DEFAULT_PREFIX = "Ressource non trouvée";

    public NotFoundHttpException(String message) {
        super(message, DEFAULT_PREFIX);
    }

    public NotFoundHttpException(String message, String prefix) {
        super(message, prefix);
    }

    @Override
    protected int defineStatusCode() {
        return 404;
    }
}
