package mg.itu.prom16.base.internal.handler;

import com.sun.jdi.InternalException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.exceptions.UnexpectedParameterException;

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

    public static void getThrowableTrace(Throwable throwable, List<Throwable> result) {
        result.add(throwable);
        if (throwable.getCause() == null) return;

        getThrowableTrace(throwable.getCause(), result);
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
