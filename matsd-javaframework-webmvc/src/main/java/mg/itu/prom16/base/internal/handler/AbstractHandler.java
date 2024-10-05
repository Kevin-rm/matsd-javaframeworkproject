package mg.itu.prom16.base.internal.handler;

import com.sun.jdi.InternalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.JsonResponse;
import mg.itu.prom16.annotations.SessionAttribute;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.exceptions.UnexpectedParameterException;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.managedinstances.NoSuchManagedInstanceException;
import mg.matsd.javaframework.core.utils.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public abstract class AbstractHandler {
    protected Class<?> controllerClass;
    protected Method method;
    protected final boolean jsonResponse;

    public AbstractHandler(Class<?> controllerClass, Method method, boolean jsonResponse) {
        this.setControllerClass(controllerClass)
            .setMethod(method);

        this.jsonResponse = jsonResponse || method.isAnnotationPresent(JsonResponse.class);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    private AbstractHandler setControllerClass(Class<?> controllerClass) {
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

    private AbstractHandler setMethod(Method method) {
        Assert.notNull(method, "La méthode ne peut pas être \"null\"");
        Assert.state(method.getDeclaringClass() == controllerClass,
            () -> new IllegalArgumentException(
                String.format("Le contrôleur \"%s\" ne dispose pas d'une méthode nommée \"%s\"", controllerClass.getName(), method.getName())
            ));

        this.method = method;
        return this;
    }

    public boolean isJsonResponse() {
        return jsonResponse;
    }

    protected abstract Object resolveAdditionalParameter(
        Class<?> parameterType, Parameter parameter, HttpServletRequest httpServletRequest, Object additionalParameter
    ) throws UnexpectedParameterException, InternalException;

    public Object invokeMethod(
        WebApplicationContainer webApplicationContainer,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Session session,
        Object additionalParameter
    ) {
        try {
            Object[] args = new Object[method.getParameterCount()];

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter     = parameters[i];
                Class<?>  parameterType = parameter.getType();

                if (parameterType == HttpServletRequest.class)
                    args[i] = httpServletRequest;
                else if (parameterType == HttpServletResponse.class)
                    args[i] = httpServletResponse;
                else if (Session.class.isAssignableFrom(parameterType))
                    args[i] = session;
                else if (parameter.isAnnotationPresent(SessionAttribute.class))
                    args[i] = UtilFunctions.getSessionAttributeValue(parameterType, parameter, httpServletRequest.getSession());
                else {
                    try {
                        args[i] = resolveAdditionalParameter(parameterType, parameter, httpServletRequest, additionalParameter);
                    } catch (UnexpectedParameterException | InternalException e) {
                        try {
                            args[i] = webApplicationContainer.getManagedInstance(parameterType);
                        } catch (NoSuchManagedInstanceException ex) {
                            throw new UnexpectedParameterException(parameter);
                        }
                    }
                }
            }

            return method.invoke(webApplicationContainer.getManagedInstance(controllerClass), args);
        } catch (IllegalAccessException e) {
            throw new InternalException();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
