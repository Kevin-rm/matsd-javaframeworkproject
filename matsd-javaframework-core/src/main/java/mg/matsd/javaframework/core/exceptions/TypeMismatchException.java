package mg.matsd.javaframework.core.exceptions;

public class TypeMismatchException extends BaseException {
    private static final String PREFIX = "Incompatibilité de type détectée";

    public TypeMismatchException(String message) {
        super(message, PREFIX);
    }
}
