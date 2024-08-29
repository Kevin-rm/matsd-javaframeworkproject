package mg.itu.prom16.base.internal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.annotations.*;
import mg.itu.prom16.exceptions.MissingServletRequestParameterException;
import mg.itu.prom16.exceptions.UndefinedPathVariableException;
import mg.itu.prom16.exceptions.UnexpectedParameterException;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringConverter;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public final class UtilFunctions {
    private UtilFunctions() { }

    public static boolean isController(@Nullable Class<?> clazz) {
        if (clazz == null) return false;

        return clazz.isAnnotationPresent(Controller.class);
    }

    public static boolean isAbsoluteUrl(@Nullable String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static Map<String, Object> getRequestMappingInfoAttributes(Method method) {
        String path = "";

        RequestMapping requestMapping = (RequestMapping) AnnotationUtils.getAnnotation(RequestMapping.class, method);
        if (method.isAnnotationPresent(RequestMapping.class))
            path = requestMapping.value();
        else if (method.isAnnotationPresent(Get.class))
            path = method.getAnnotation(Get.class).value();
        else if (method.isAnnotationPresent(Post.class))
            path = method.getAnnotation(Post.class).value();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", path);
        attributes.put("methods", requestMapping.methods());

        return attributes;
    }

    @Nullable
    public static Object getRequestParameterValue(
        Class<?>  parameterType,
        Parameter parameter,
        HttpServletRequest httpServletRequest
    ) {
        if (Map.class.isAssignableFrom(parameterType))
            return getRequestParameterMap(parameter, httpServletRequest);

        RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
        String parameterName = StringUtils.hasText(requestParameter.name()) ? requestParameter.name() : parameter.getName();

        String parameterValue = httpServletRequest.getParameter(parameterName);
        if (parameterValue == null || StringUtils.isBlank(parameterValue)) {
            if (StringUtils.hasText(requestParameter.defaultValue()))
                return StringConverter.convert(requestParameter.defaultValue(), parameterType);
            else if (requestParameter.required())
                throw new MissingServletRequestParameterException(parameterName);
            else if (parameterType.isPrimitive())
                return ClassUtils.getPrimitiveDefaultValue(parameterType);
            else if (!ClassUtils.isPrimitiveWrapper(parameterType) && parameterType != String.class)
                throw new UnexpectedParameterException(parameter);

            return null;
        }

        return StringConverter.convert(parameterValue, parameterType);
    }

    public static Object getPathVariableValue(
        Class<?>  parameterType,
        Parameter parameter,
        RequestMappingInfo requestMappingInfo,
        HttpServletRequest httpServletRequest
    ) {
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        String pathVariableName = StringUtils.hasText(pathVariable.value()) ? pathVariable.value() : parameter.getName();

        Map<String, String> pathVariables = requestMappingInfo.extractPathVariablesValues(httpServletRequest);
        if (!pathVariables.containsKey(pathVariableName))
            throw new UndefinedPathVariableException(pathVariableName, requestMappingInfo);

        return StringConverter.convert(pathVariables.get(pathVariableName), parameterType);
    }

    public static Object bindRequestParameters(Class<?> parameterType, Parameter parameter, HttpServletRequest httpServletRequest) {
        String modelName = null;
        if (parameter.isAnnotationPresent(FromRequestParameters.class))
            modelName = parameter.getAnnotation(FromRequestParameters.class).value();
        if (modelName == null || StringUtils.isBlank(modelName))
            modelName = parameter.getName();

        return instantiateModelFromRequest(parameterType, modelName, httpServletRequest);
    }

    public static Object getSessionAttributeValue(
        Class<?>    parameterType,
        Parameter   parameter,
        HttpSession httpSession
    ) {
        SessionAttribute sessionAttribute = parameter.getAnnotation(SessionAttribute.class);
        String sessionAttributeName = StringUtils.hasText(sessionAttribute.value()) ? sessionAttribute.value() : parameter.getName();

        Object sessionAttributeValue = httpSession.getAttribute(sessionAttributeName);
        if (sessionAttributeValue != null && !ClassUtils.isAssignable(parameterType, sessionAttributeValue.getClass())) {
            Executable executable = parameter.getDeclaringExecutable();

            throw new ClassCastException(String.format(
                "L'attribut de session \"%s\" est de type \"%s\" mais le paramètre \"%s\" annoté de la méthode \"%s\" " +
                    "du contrôleur \"%s\" est de type \"%s\"",
                sessionAttributeName, sessionAttributeValue.getClass(), parameter.getName(),
                executable.getName(), executable.getDeclaringClass().getName(),
                parameterType
            ));
        }

        return sessionAttributeValue;
    }

    private static Object instantiateModelFromRequest(Class<?> clazz, String modelName, HttpServletRequest httpServletRequest) {
        try {
            Object result = clazz.getConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                BindRequestParameter bindRequestParameter = field.getAnnotation(BindRequestParameter.class);
                String fieldAlias = bindRequestParameter != null && StringUtils.hasText(bindRequestParameter.value()) ?
                    bindRequestParameter.value() : field.getName();

                String requestParameterValue = httpServletRequest.getParameter(modelName + "." + fieldAlias);
                if (requestParameterValue == null || StringUtils.isBlank(requestParameterValue)) continue;

                if (
                    ClassUtils.isPrimitiveOrWrapper(fieldType) ||
                    ClassUtils.isStandardClass(fieldType)      ||
                    fieldType == String.class
                )    field.set(result, StringConverter.convert(requestParameterValue, fieldType));
                else field.set(result, instantiateModelFromRequest(fieldType, fieldAlias, httpServletRequest));
            }

            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String[]> getRequestParameterMap(
        Parameter parameter,
        HttpServletRequest httpServletRequest
    ) {
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException(String.format(
           "Le type Map attendu du paramètre \"%s\" est \"Map<String, String[]>\" ou une de ses sous-classes de cette structure " +
           "mais vous avez donné \"%s\"", parameter.getName(), parameter.getParameterizedType()
        ));

        if (parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (
                typeArguments.length != 2        ||
                typeArguments[0] != String.class ||
                typeArguments[1] != String[].class
            ) throw illegalArgumentException;

            return httpServletRequest.getParameterMap();
        }

        throw illegalArgumentException;
    }
}
