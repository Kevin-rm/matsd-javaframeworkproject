package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

import java.lang.reflect.Parameter;

public class UnsupportedParameterTypeException extends BaseException {
    private static final String PREFIX = "Type de paramètre non supporté";

    public UnsupportedParameterTypeException(Parameter parameter) {
        super(String.format("%s pour le paramètre nommé \"%s\"", parameter.getType(), parameter.getName()), PREFIX);
    }
}
