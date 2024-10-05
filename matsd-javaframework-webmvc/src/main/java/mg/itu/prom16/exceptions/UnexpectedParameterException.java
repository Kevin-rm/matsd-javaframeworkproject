package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

import java.lang.reflect.Parameter;

public class UnexpectedParameterException extends BaseException {
    private static final String PREFIX = "Type de paramètre inattendu";

    public UnexpectedParameterException() { }

    public UnexpectedParameterException(Parameter parameter) {
        super(String.format("\"%s %s\"", parameter.getType(), parameter.getName()), PREFIX);
    }
}
