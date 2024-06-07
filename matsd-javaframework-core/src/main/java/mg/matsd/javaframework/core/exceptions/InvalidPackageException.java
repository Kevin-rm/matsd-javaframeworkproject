package mg.matsd.javaframework.core.exceptions;

public class InvalidPackageException extends BaseException {
    private static final String PREFIX = "Nom de package invalide";

    public InvalidPackageException(String message) {
        super(message, PREFIX);
    }
}
