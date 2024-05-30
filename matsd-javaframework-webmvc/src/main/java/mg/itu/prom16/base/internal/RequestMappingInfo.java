package mg.itu.prom16.base.internal;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.RequestMethod;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class RequestMappingInfo {
    private String path;
    private RequestMethod[] methods;

    public RequestMappingInfo(@Nullable String path, @Nullable RequestMethod[] methods) {
        this.setPath(path)
            .setMethods(methods);
    }

    public String getPath() {
        return path;
    }

    public RequestMappingInfo setPath(@Nullable String path) {
        if (path == null || StringUtils.isBlank(path)) {
            this.path = "/";

            return this;
        }

        path = path.strip();
        if (!path.startsWith("/"))
            throw new IllegalArgumentException("L'argument path doit commencer par un slash \"/\"");

        if (path.length() > 1 && path.endsWith("/"))
            path = path.replaceAll("/+$", "");

        this.path = path;
        return this;
    }

    public RequestMethod[] getMethods() {
        return methods;
    }

    public RequestMappingInfo setMethods(@Nullable RequestMethod[] methods) {
        if (methods == null || methods.length == 0)
            methods = new RequestMethod[]{RequestMethod.GET};

        this.methods = methods;
        return this;
    }

    public boolean matches(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        return path.equals(httpServletRequest.getServletPath()) &&
               Arrays.asList(methods).contains(RequestMethod.valueOf(httpServletRequest.getMethod()));
    }

    @Override
    public String toString() {
        return "RequestMappingInfo{" +
            "path='" + path + '\'' +
            ", methods=" + Arrays.toString(methods) +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestMappingInfo that = (RequestMappingInfo) o;

        if (!Objects.equals(path, that.path)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(methods, that.methods);
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(methods);
        return result;
    }
}
