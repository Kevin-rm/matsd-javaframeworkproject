package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class TransactionException extends BaseException {
    private static final String PREFIX = "Erreur rencontrée durant une transaction";

    public TransactionException(String message, Throwable cause) {
        super(message, PREFIX, cause);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }
}
