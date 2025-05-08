package mg.matsd.javaframework.security.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public abstract class HttpStatusException extends BaseException {
    private final int statusCode = defineStatusCode();

    public HttpStatusException(String message) {
        super(message);
    }

    public HttpStatusException(String message, String prefix) {
        super(message, prefix);
    }

    public int getStatusCode() {
        return statusCode;
    }

    protected abstract int defineStatusCode();
}
