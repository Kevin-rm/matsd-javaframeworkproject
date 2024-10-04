package mg.itu.prom16.base.internal.handler;

import com.sun.jdi.InternalException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.exceptions.UnexpectedParameterException;
import mg.matsd.javaframework.core.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ExceptionHandler extends AbstractHandler {
    private final List<Class<? extends Throwable>> exceptionsClasses;
    private final boolean isGlobal;

    public ExceptionHandler(
        Class<?> controllerClass, Method method, boolean jsonResponse, Class<? extends Throwable>[] exceptionsClasses, boolean isGlobal
    ) {
        super(controllerClass, method, jsonResponse);
        this.exceptionsClasses = List.of(exceptionsClasses);
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean canHandle(List<Throwable> throwableTrace, Class<?> currentControllerClass) {
        return throwableTrace.stream().anyMatch(throwable ->
            exceptionsClasses.stream().anyMatch(exceptionClass ->
                exceptionClass.isInstance(throwable)
            )
        ) && (isGlobal || controllerClass.equals(currentControllerClass));
    }

    public static List<Throwable> getThrowableTrace(Throwable throwable, @Nullable List<Throwable> result) {
        if (result == null) result = new ArrayList<>();

        result.add(throwable);
        if (throwable.getCause() == null) return result;

        return getThrowableTrace(throwable.getCause(), result);
    }

    @Override
    protected Object resolveAdditionalParameter(
        Class<?> parameterType, Parameter parameter, HttpServletRequest httpServletRequest, Object additionalParameter
    ) throws UnexpectedParameterException, InternalException {
        if (!(additionalParameter instanceof List<?> list)) throw new InternalException();

        for (Object object : list) {
            Throwable throwable = (Throwable) object;

            if (parameterType != throwable.getClass()) continue;
            return throwable;
        }

        throw new UnexpectedParameterException();
    }
}
