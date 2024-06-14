package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class MissingServletRequestParameterException extends BaseException {
    public MissingServletRequestParameterException(String missingParameter) {
        super(String.format("Le paramètre \"%s\" est requis mais n'a pas été spécifié", missingParameter));
    }
}
