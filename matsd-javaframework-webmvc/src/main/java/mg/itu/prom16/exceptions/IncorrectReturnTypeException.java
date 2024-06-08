package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

import java.lang.reflect.Method;

public class IncorrectReturnTypeException extends BaseException {
    private static final String PREFIX = "Type de retour incorrect";

    public IncorrectReturnTypeException(Method controllerMethod) {
        super(String.format(
            "La méthode \"%s\" dans le contrôleur \"%s\" doit avoir comme type de retour \"ModelView\" ou \"String\", " +
                "mais vous avez donné : \"%s\"",
            controllerMethod.getName(),
            controllerMethod.getDeclaringClass().getName(),
            controllerMethod.getReturnType()
        ), PREFIX);
    }
}
