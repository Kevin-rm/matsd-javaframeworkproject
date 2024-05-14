package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class InvalidPackageException extends BaseException {
    private static final String PREFIX = "Package non valide";

    public InvalidPackageException(String packageName) {
        super(
            String.format("\"%s\" est soit introuvable, soit inaccessible dans le classpath ou soit ne correspond pas à un dossier", packageName),
            PREFIX
        );
    }
}
