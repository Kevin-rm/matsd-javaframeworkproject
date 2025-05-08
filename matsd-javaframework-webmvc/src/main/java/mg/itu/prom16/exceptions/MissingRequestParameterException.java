package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class MissingRequestParameterException extends BaseException {
    private final String missingParameter;

    public MissingRequestParameterException(String missingParameter) {
        super(String.format("Le paramètre \"%s\" est requis mais n'a pas été spécifié", missingParameter));
        this.missingParameter = missingParameter;
    }

    public String getMissingParameter() {
        return missingParameter;
    }
}
