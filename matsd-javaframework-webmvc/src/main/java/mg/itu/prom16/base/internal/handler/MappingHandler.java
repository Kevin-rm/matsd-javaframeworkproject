package mg.itu.prom16.base.internal.handler;

import com.sun.jdi.InternalException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.ModelData;
import mg.itu.prom16.annotations.PathVariable;
import mg.itu.prom16.annotations.RequestParameter;
import mg.itu.prom16.base.Model;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.exceptions.UnexpectedParameterException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MappingHandler extends AbstractHandler {
    public MappingHandler(Class<?> controllerClass, Method method, boolean jsonResponse) {
        super(controllerClass, method, jsonResponse);
    }

    @Override
    protected Object resolveAdditionalParameter(
        Class<?> parameterType, Parameter parameter,
        HttpServletRequest httpServletRequest, Model model, Object additionalParameter
    ) throws UnexpectedParameterException, InternalException, ServletException {
        if (!(additionalParameter instanceof RequestMappingInfo requestMappingInfo)) throw new InternalException();

        Object result;
        if (parameter.isAnnotationPresent(RequestParameter.class))
            result = UtilFunctions.getRequestParameterValue(parameterType, parameter, httpServletRequest);
        else if (parameter.isAnnotationPresent(PathVariable.class))
            result = UtilFunctions.getPathVariableValue(parameterType, parameter, requestMappingInfo, httpServletRequest);
        else if (parameter.isAnnotationPresent(ModelData.class))
            result = UtilFunctions.bindRequestParameters(parameterType, parameter, httpServletRequest, model);
        else throw new UnexpectedParameterException();

        return result;
    }
}
