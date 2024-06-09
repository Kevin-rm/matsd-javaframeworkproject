package mg.itu.prom16.base.internal;

import com.sun.jdi.InternalException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.PathVariable;
import mg.itu.prom16.annotations.RequestParameter;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.utils.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MappingHandler {
    private Class<?> controllerClass;
    private Method   method;

    public MappingHandler(Class<?> controllerClass, Method method) {
        this.setControllerClass(controllerClass)
            .setMethod(method);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    private MappingHandler setControllerClass(Class<?> controllerClass) {
        Assert.notNull(controllerClass, "La classe du contrôleur ne peut pas être \"null\"");
        Assert.state(UtilFunctions.isController(controllerClass),
            () -> new IllegalArgumentException("La classe passée en argument n'est pas un contrôleur")
        );

        this.controllerClass = controllerClass;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    private MappingHandler setMethod(Method method) {
        Assert.notNull(method, "La méthode associée au path ne peut pas être \"null\"");
        Assert.state(method.getDeclaringClass() == controllerClass,
            () -> new IllegalArgumentException(
                String.format("Le contrôleur \"%s\" ne dispose pas d'une méthode nommée \"%s\"", controllerClass.getName(), method.getName())
            ));

        this.method = method;
        return this;
    }

    public Object invokeMethod(
        WebApplicationContainer webApplicationContainer,
        HttpServletRequest httpServletRequest,
        RequestMappingInfo requestMappingInfo
    ) {
        try {
            Object[] args = new Object[method.getParameterCount()];

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter     = parameters[i];
                Class<?>  parameterType = parameter.getType();

                if (parameterType == HttpServletRequest.class)
                    args[i] = httpServletRequest;
                else if (parameter.isAnnotationPresent(RequestParameter.class))
                    args[i] = UtilFunctions.getRequestParameterValue(parameterType, parameter, httpServletRequest);
                else if (parameter.isAnnotationPresent(PathVariable.class))
                    args[i] = UtilFunctions.getPathVariableValue(parameterType, parameter, requestMappingInfo, httpServletRequest);
            }

            return method.invoke(
                webApplicationContainer.getManagedInstance(controllerClass), args
            );
        } catch (IllegalAccessException e) {
            throw new InternalException();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "MappingHandler{" +
            "controllerClass=" + controllerClass +
            ", method=" + method +
            '}';
    }
}
