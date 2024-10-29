package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ModelBindingException extends BaseException {
    private static final String PREFIX = "Erreur lors d'un binding de model";

    public ModelBindingException(Throwable cause) {
      super(PREFIX, cause);
    }
}
