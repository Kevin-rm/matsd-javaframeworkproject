package mg.itu.prom16.upload;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class FileUploadException extends BaseException {
    private static final String PREFIX = "Erreur lors d'un upload de fichier";

    public FileUploadException(String message) {
        super(message, PREFIX);
    }

    public FileUploadException(Throwable cause) {
        super(PREFIX, cause);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, PREFIX, cause);
    }
}
