package mg.itu.prom16.exceptions;

import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.core.exceptions.BaseException;

public class NotFoundHttpException extends BaseException {
    public  static final int statusCode = HttpServletResponse.SC_NOT_FOUND;
    private static final String PREFIX  = "Ressource non trouvée";

    public NotFoundHttpException(String message) {
        super(message, PREFIX);
    }
}
