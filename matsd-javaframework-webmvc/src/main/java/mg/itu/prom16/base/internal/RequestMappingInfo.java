package mg.itu.prom16.base.internal;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.RequestMethod;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.*;

public class RequestMappingInfo {
    private String path;
    private List<RequestMethod> methods;

    public RequestMappingInfo(@Nullable String path, @Nullable List<RequestMethod> methods) {
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

    public List<RequestMethod> getMethods() {
        return methods;
    }

    public RequestMappingInfo setMethods(@Nullable List<RequestMethod> methods) {
        if (methods == null || methods.isEmpty()) {
            methods = new ArrayList<>();
            methods.add(RequestMethod.GET);
        }

        this.methods = methods;
        return this;
    }

    public boolean matches(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        return path.equals(httpServletRequest.getServletPath()) &&
               methods.contains(
                   RequestMethod.valueOf(httpServletRequest.getMethod())
               );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestMappingInfo that = (RequestMappingInfo) o;

        if (!Objects.equals(path, that.path)) return false;
        return Objects.equals(methods, that.methods);
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (methods != null ? methods.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RequestMappingInfo{" +
            "path='" + path + '\'' +
            ", methods=" + methods +
            '}';
    }
}
