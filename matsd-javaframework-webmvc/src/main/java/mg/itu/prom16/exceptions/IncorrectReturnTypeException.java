package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

import java.lang.reflect.Method;

public class IncorrectReturnTypeException extends BaseException {
    private static final String PREFIX = "Type de retour incorrect";

    public IncorrectReturnTypeException(Method controllerMethod) {
        super(String.format(
            "La méthode \"%s\" dans le contrôleur \"%s\" doit avoir un type de retour \"ModelView\" ou \"String\"",
            controllerMethod.getName(),
            controllerMethod.getReturnType()
        ), PREFIX);
    }
}
