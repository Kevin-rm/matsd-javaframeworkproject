package mg.itu.prom16.base.internal;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import mg.itu.prom16.annotations.*;
import mg.itu.prom16.base.Model;
import mg.itu.prom16.validation.GlobalError;
import mg.itu.prom16.validation.ModelBindingResult;
import mg.itu.prom16.exceptions.MissingServletRequestParameterException;
import mg.itu.prom16.exceptions.ModelBindingException;
import mg.itu.prom16.exceptions.UndefinedPathVariableException;
import mg.itu.prom16.exceptions.UnexpectedParameterException;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.upload.FileUploadException;
import mg.itu.prom16.upload.UploadedFile;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringConverter;
import mg.matsd.javaframework.validation.annotations.Validate;
import mg.matsd.javaframework.validation.base.ValidatorFactory;

import java.io.IOException;
import java.lang.reflect.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class UtilFunctions {
    private static final Set<Class<?>> ALLOWED_CLASSES = Set.of(
        LocalDate.class, LocalDateTime.class, LocalTime.class, Date.class, Timestamp.class, Time.class);

    private UtilFunctions() { }

    public static boolean isController(@Nullable Class<?> clazz) {
        if (clazz == null) return false;

        return AnnotationUtils.hasAnnotation(Controller.class, clazz);
    }

    public static boolean isAbsoluteUrl(@Nullable String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static Map<String, Object> getRequestMappingInfoAttributes(Method method) {
        String path = "";
        String name = "";

        RequestMapping requestMapping = (RequestMapping) AnnotationUtils.getAnnotation(RequestMapping.class, method);
        if (method.isAnnotationPresent(RequestMapping.class)) {
            path = requestMapping.value();
            name = requestMapping.name();
        } else if (method.isAnnotationPresent(Get.class)) {
            Get get = method.getAnnotation(Get.class);

            path = get.value();
            name = get.name();
        } else if (method.isAnnotationPresent(Post.class)) {
            Post post = method.getAnnotation(Post.class);

            path = post.value();
            name = post.name();
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", path);
        attributes.put("name", name);
        attributes.put("methods", requestMapping.methods());

        return attributes;
    }

    @Nullable
    public static Object getRequestParameterValue(
        Class<?>  parameterType,
        Parameter parameter,
        HttpServletRequest httpServletRequest
    ) throws ServletException {
        if (Map.class.isAssignableFrom(parameterType))
            return getRequestParameterMap(parameter, httpServletRequest);

        RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
        String parameterName = StringUtils.hasText(requestParameter.name()) ? requestParameter.name() : parameter.getName();

        if (UploadedFile.class == parameterType) {
            UploadedFile uploadedFile = getUploadedFile(parameterName, httpServletRequest);
            if (uploadedFile == null && requestParameter.required())
                throw new MissingServletRequestParameterException(parameterName);

            return uploadedFile;
        }

        String parameterValue = httpServletRequest.getParameter(parameterName);
        if (parameterValue == null || StringUtils.isBlank(parameterValue)) {
            if (StringUtils.hasText(requestParameter.defaultValue()))
                return StringConverter.convert(requestParameter.defaultValue(), parameterType);
            else if (requestParameter.required())
                throw new MissingServletRequestParameterException(parameterName);
            else if (parameterType.isPrimitive())
                return ClassUtils.getPrimitiveDefaultValue(parameterType);
            else if (!ClassUtils.isPrimitiveWrapper(parameterType) &&
                parameterType != String.class &&
                !ALLOWED_CLASSES.contains(parameterType)
            ) throw new UnexpectedParameterException(parameter);

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

    @Nullable
    public static Object bindRequestParameters(
        Class<?> parameterType, Parameter parameter,
        WebApplicationContainer webApplicationContainer, HttpServletRequest httpServletRequest
    ) {
        Model model = (Model) webApplicationContainer.getManagedInstance(Model.MANAGED_INSTANCE_ID);

        String modelName = parameter.getAnnotation(ModelData.class).value();
        if (modelName == null || StringUtils.isBlank(modelName))
            modelName = parameter.getName();

        Object result = null;
        ModelBindingResult modelBindingResult = (ModelBindingResult) webApplicationContainer.getManagedInstance(ModelBindingResult.MANAGED_INSTANCE_ID);
        try {
            Object modelInstance;
            if (!model.hasData(modelName)) {
                modelInstance = instantiateModel(parameterType);
                model.addData(modelName, modelInstance);
            } else modelInstance = model.getData(modelName);

            result = populateModelFromRequest(parameterType, modelInstance, modelName, httpServletRequest);
            if (parameter.isAnnotationPresent(Validate.class))
                modelBindingResult.addValidationErrors(modelName, ((ValidatorFactory) webApplicationContainer
                    .getManagedInstance(ValidatorFactory.class))
                    .getValidator()
                    .doValidate(modelInstance));
        } catch (Throwable e) {
            modelBindingResult.addGlobalError(e);
        }

        return result;
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

    private static Object instantiateModel(Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException e) {
            throw new ModelBindingException(e instanceof InvocationTargetException ? e.getCause() : e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new ModelBindingException(String.format("Le modèle avec la classe \"%s\" doit disposer d'un constructeur sans arguments " +
                "accessible publiquement", clazz.getName()));
        }
    }

    private static Object populateModelFromRequest(
        Class<?> clazz, @Nullable Object modelInstance, String modelName, HttpServletRequest httpServletRequest
    ) {
        if (modelInstance == null) modelInstance = instantiateModel(clazz);
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                BindRequestParameter bindRequestParameter = field.getAnnotation(BindRequestParameter.class);
                String fieldAlias = bindRequestParameter != null && StringUtils.hasText(bindRequestParameter.value()) ?
                    bindRequestParameter.value() : field.getName();

                String requestParameterName = modelName + "." + fieldAlias;
                if (fieldType == UploadedFile.class) {
                    UploadedFile uploadedFile = getUploadedFile(requestParameterName, httpServletRequest);
                    if (uploadedFile != null) field.set(modelInstance, uploadedFile);

                    continue;
                }

                String requestParameterValue = httpServletRequest.getParameter(requestParameterName);
                if (requestParameterValue == null || StringUtils.isBlank(requestParameterValue)) continue;

                if (
                    ClassUtils.isPrimitiveOrWrapper(fieldType) ||
                    ClassUtils.isStandardClass(fieldType)      ||
                    fieldType == String.class
                )    field.set(modelInstance, StringConverter.convert(requestParameterValue, fieldType));
                else field.set(modelInstance, populateModelFromRequest(fieldType, null, fieldAlias, httpServletRequest));
            }

            return modelInstance;
        } catch (ServletException | IllegalAccessException e) {
            throw new ModelBindingException(e);
        }
    }

    @Nullable
    private static UploadedFile getUploadedFile(String parameterName, HttpServletRequest httpServletRequest)
        throws ServletException {
        String contentType = httpServletRequest.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data"))
            return null;

        try {
            Part part = httpServletRequest.getPart(parameterName);
            if (part == null)
                return null;

            return new UploadedFile(part);
        } catch (IOException e) {
            throw new FileUploadException(e);
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
