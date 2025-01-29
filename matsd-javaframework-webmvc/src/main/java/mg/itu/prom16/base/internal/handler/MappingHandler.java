package mg.itu.prom16.base.internal.handler;

import com.sun.jdi.InternalException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.ModelData;
import mg.itu.prom16.annotations.PathVariable;
import mg.itu.prom16.annotations.RequestParameter;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.exceptions.UnexpectedParameterException;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.annotation.Anonymous;
import mg.matsd.javaframework.security.annotation.Authorize;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MappingHandler extends AbstractHandler {
    @Nullable
    private List<String> allowedRoles;
    private boolean anonymous;

    public MappingHandler(
        Class<?> controllerClass,
        Method method,
        boolean jsonResponse,
        @Nullable String[] sharedAllowedRoles,
        boolean anonymous
    ) {
        super(controllerClass, method, jsonResponse);
        this.setAllowedRoles(sharedAllowedRoles)
            .setAnonymous(anonymous);
    }

    @Nullable
    public List<String> getAllowedRoles() {
        return allowedRoles;
    }

    private MappingHandler setAllowedRoles(@Nullable String[] sharedAllowedRoles) {
        if (sharedAllowedRoles == null && !method.isAnnotationPresent(Authorize.class)) return this;

        allowedRoles = new ArrayList<>();

        String[] roles = method.getAnnotation(Authorize.class).value();
        final String[] finalRoles = sharedAllowedRoles != null ? Stream.concat(Arrays.stream(roles), Arrays.stream(sharedAllowedRoles))
            .toArray(String[]::new) : roles;
        if (finalRoles.length == 0) return this;

        Arrays.stream(finalRoles).forEachOrdered(role -> {
            Assert.notBlank(role, false, "Chaque rôle ne peut pas être vide ou \"null\"");
            allowedRoles.add(role);
        });

        return this;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    private MappingHandler setAnonymous(boolean anonymous) {
        if (allowedRoles != null) return this;

        this.anonymous = anonymous || method.isAnnotationPresent(Anonymous.class);
        return this;
    }

    @Override
    protected Object resolveAdditionalParameter(
        Class<?> parameterType, Parameter parameter,
        WebApplicationContainer webApplicationContainer, HttpServletRequest httpServletRequest,
        Object additionalParameter
    ) throws UnexpectedParameterException, InternalException, ServletException {
        if (!(additionalParameter instanceof RequestMappingInfo requestMappingInfo)) throw new InternalException();

        Object result;
        if (parameter.isAnnotationPresent(RequestParameter.class))
            result = UtilFunctions.getRequestParameterValue(parameterType, parameter, httpServletRequest);
        else if (parameter.isAnnotationPresent(PathVariable.class))
            result = UtilFunctions.getPathVariableValue(parameterType, parameter, requestMappingInfo, httpServletRequest);
        else if (parameter.isAnnotationPresent(ModelData.class))
            result = UtilFunctions.bindRequestParameters(parameterType, parameter, webApplicationContainer, httpServletRequest);
        else throw new UnexpectedParameterException();

        return result;
    }
}
