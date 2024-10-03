package mg.itu.prom16.base.internal.handler;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.FromRequestParameters;
import mg.itu.prom16.annotations.PathVariable;
import mg.itu.prom16.annotations.RequestParameter;
import mg.itu.prom16.annotations.SessionAttribute;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MappingHandler extends AbstractHandler {

    public MappingHandler(Class<?> controllerClass, Method method, boolean jsonResponse) {
        super(controllerClass, method, jsonResponse);
    }

    @Override
    protected Object resolveAdditionalParameter(
        Class<?> parameterType, Parameter parameter, HttpServletRequest httpServletRequest, Object additionalParameter
    ) throws RuntimeException {
        if (!(additionalParameter instanceof RequestMappingInfo requestMappingInfo)) throw new RuntimeException();

        Object result;
        if (parameter.isAnnotationPresent(SessionAttribute.class))
            result = UtilFunctions.getSessionAttributeValue(parameterType, parameter, httpServletRequest.getSession());
        else if (parameter.isAnnotationPresent(RequestParameter.class))
            result = UtilFunctions.getRequestParameterValue(parameterType, parameter, httpServletRequest);
        else if (parameter.isAnnotationPresent(PathVariable.class))
            result = UtilFunctions.getPathVariableValue(parameterType, parameter, requestMappingInfo, httpServletRequest);
        else if (parameter.isAnnotationPresent(FromRequestParameters.class))
            result  = UtilFunctions.bindRequestParameters(parameterType, parameter, httpServletRequest);
        else throw new RuntimeException();

        return result;
    }
}
