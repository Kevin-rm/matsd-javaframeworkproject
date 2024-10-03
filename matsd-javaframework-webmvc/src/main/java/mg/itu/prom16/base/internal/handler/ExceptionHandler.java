package mg.itu.prom16.base.internal.handler;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class ExceptionHandler extends AbstractHandler {
    private List<Class<? extends Throwable>> exceptionsClasses;
    private final boolean isGlobal;

    public ExceptionHandler(
        Class<?> controllerClass, Method method, boolean jsonResponse, Class<? extends Throwable>[] exceptionsClasses, boolean isGlobal
    ) {
        super(controllerClass, method, jsonResponse);
        setExceptionsClasses(exceptionsClasses);
        this.isGlobal = isGlobal;
    }

    public List<Class<? extends Throwable>> getExceptionsClasses() {
        return exceptionsClasses;
    }

    private void setExceptionsClasses(Class<? extends Throwable>[] exceptionsClasses) {
        this.exceptionsClasses = List.of(exceptionsClasses);
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    protected Object resolveAdditionalParameter(
        Class<?> parameterType, Parameter parameter, HttpServletRequest httpServletRequest, Object additionalParameter
    ) throws RuntimeException {
        if (!(additionalParameter instanceof Throwable throwable) || parameterType != throwable.getClass())
            throw new RuntimeException();

        return additionalParameter;
    }
}
