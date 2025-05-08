package mg.itu.prom16.base.internal.handler;

import com.sun.jdi.InternalException;
import jakarta.servlet.ServletException;
import mg.itu.prom16.annotations.JsonResponse;
import mg.itu.prom16.annotations.SessionAttribute;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.exceptions.UnexpectedParameterException;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.di.exceptions.NoSuchManagedInstanceException;
import mg.matsd.javaframework.security.base.AuthenticationManager;
import mg.matsd.javaframework.security.base.Security;
import mg.matsd.javaframework.servletwrapper.http.Request;
import mg.matsd.javaframework.servletwrapper.http.Response;
import mg.matsd.javaframework.servletwrapper.http.Session;
import mg.matsd.javaframework.validation.base.Validator;
import mg.matsd.javaframework.validation.base.ValidatorFactory;

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
        Assert.isTrue(UtilFunctions.isController(controllerClass),
            "La classe passée en argument n'est pas un contrôleur");

        this.controllerClass = controllerClass;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    private AbstractHandler setMethod(Method method) {
        Assert.notNull(method, "La méthode ne peut pas être \"null\"");
        Assert.isTrue(method.getDeclaringClass() == controllerClass,
            String.format("Le contrôleur \"%s\" ne dispose pas d'une méthode nommée \"%s\"", controllerClass.getName(), method.getName()
        ));

        this.method = method;
        return this;
    }

    public boolean isJsonResponse() {
        return jsonResponse;
    }

    protected abstract Object resolveAdditionalParameter(
        Class<?> parameterType, Parameter parameter,
        WebApplicationContainer webApplicationContainer, Request request,
        Object additionalParameter
    ) throws UnexpectedParameterException, InternalException, ServletException;

    public Object invokeMethod(
        WebApplicationContainer webApplicationContainer,
        Request  request,
        Response response,
        Session  session,
        Object additionalParameter
    ) throws ServletException {
        try {
            Object[] args = new Object[method.getParameterCount()];

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter     = parameters[i];
                Class<?>  parameterType = parameter.getType();

                if (parameterType == Request.class)
                    args[i] = request;
                else if (parameterType == Response.class)
                    args[i] = response;
                else if (parameterType == Session.class)
                    args[i] = session;
                else if (parameter.isAnnotationPresent(SessionAttribute.class))
                    args[i] = UtilFunctions.getSessionAttributeValue(parameterType, parameter, session);
                else if (parameterType == Validator.class)
                    args[i] = webApplicationContainer.getManagedInstance(ValidatorFactory.class).getValidator();
                else if (parameterType == AuthenticationManager.class)
                    args[i] = webApplicationContainer.getManagedInstance(Security.class).getAuthenticationManager();
                else {
                    try {
                        args[i] = resolveAdditionalParameter(parameterType, parameter, webApplicationContainer, request, additionalParameter);
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
            throw new ServletException(e.getCause());
        }
    }
}
