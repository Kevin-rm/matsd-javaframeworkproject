package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class UnsupportedParameterTypeException extends BaseException {
    private static final String PREFIX = "Type de paramètre non supporté";

    public UnsupportedParameterTypeException(Class<?> parameterType, String parameterName) {
        super(String.format("%s pour le paramètre nommé \"%s\"", parameterType, parameterName), PREFIX);
    }
}
