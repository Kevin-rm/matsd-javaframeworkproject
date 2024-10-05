package mg.itu.prom16.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

import java.lang.reflect.Method;

public class InvalidReturnTypeException extends BaseException {
    private static final String PREFIX = "Type de retour invalide";

    public InvalidReturnTypeException(String message) {
        super(message);
    }

    public InvalidReturnTypeException(Method handlerMethod) {
        super(String.format(
            "La méthode \"%s\" dans le contrôleur \"%s\" doit avoir comme type de retour : \"ModelAndView\", \"RedirectView\", ou \"String\", " +
                "mais vous avez donné \"%s\"",
            handlerMethod.getName(),
            handlerMethod.getDeclaringClass().getName(),
            handlerMethod.getReturnType()
        ), PREFIX);
    }
}
