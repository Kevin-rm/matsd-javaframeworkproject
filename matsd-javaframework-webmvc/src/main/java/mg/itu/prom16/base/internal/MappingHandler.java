package mg.itu.prom16.base.internal;

import com.sun.jdi.InternalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.FromRequestParameters;
import mg.itu.prom16.annotations.PathVariable;
import mg.itu.prom16.annotations.RequestParameter;
import mg.itu.prom16.annotations.SessionAttribute;
import mg.itu.prom16.base.ModelView;
import mg.itu.prom16.exceptions.UnexpectedParameterException;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.managedinstances.NoSuchManagedInstanceException;
import mg.matsd.javaframework.core.utils.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MappingHandler {
    private Class<?> controllerClass;
    private Method   method;
    private boolean  jsonResponse;

    public MappingHandler(Class<?> controllerClass, Method method, boolean jsonResponse) {
        this.setControllerClass(controllerClass)
            .setMethod(method)
            .setJsonResponse(jsonResponse);
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

    public boolean isJsonResponse() {
        return jsonResponse;
    }

    private MappingHandler setJsonResponse(boolean jsonResponse) {
        Class<?> returnType = method.getReturnType();
        if (returnType == ModelView.class)
            throw new IllegalArgumentException("Impossible d'envoyer une réponse sous le format \"JSON\" si le type de retour est \"ModelView\"");

        this.jsonResponse = jsonResponse;
        return this;
    }

    public Object invokeMethod(
        WebApplicationContainer webApplicationContainer,
        HttpServletRequest  httpServletRequest,
        HttpServletResponse httpServletResponse,
        Session session,
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
                else if (parameterType == HttpServletResponse.class)
                    args[i] = httpServletResponse;
                else if (Session.class.isAssignableFrom(parameterType))
                    args[i] = session;
                else if (parameter.isAnnotationPresent(RequestParameter.class))
                    args[i] = UtilFunctions.getRequestParameterValue(parameterType, parameter, httpServletRequest);
                else if (parameter.isAnnotationPresent(PathVariable.class))
                    args[i] = UtilFunctions.getPathVariableValue(parameterType, parameter, requestMappingInfo, httpServletRequest);
                else if (parameter.isAnnotationPresent(FromRequestParameters.class))
                    args[i] = UtilFunctions.bindRequestParameters(parameterType, parameter, httpServletRequest);
                else if (parameter.isAnnotationPresent(SessionAttribute.class))
                    args[i] = UtilFunctions.getSessionAttributeValue(parameterType, parameter, httpServletRequest.getSession());
                else try {
                        args[i] = webApplicationContainer.getManagedInstance(parameterType);
                     } catch (NoSuchManagedInstanceException ignored) {
                        throw new UnexpectedParameterException(parameter);
                     }
            }

            return method.invoke(webApplicationContainer.getManagedInstance(controllerClass), args);
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
