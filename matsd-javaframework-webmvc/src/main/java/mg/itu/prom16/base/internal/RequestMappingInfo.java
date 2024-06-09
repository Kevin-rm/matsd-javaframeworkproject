package mg.itu.prom16.base.internal;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.RequestMethod;
import mg.itu.prom16.exceptions.DuplicatePathVariableNameException;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestMappingInfo {
    private String path;
    private List<RequestMethod> methods;
    private List<String> pathVariableNames;
    private Pattern pathPattern;

    public RequestMappingInfo(@Nullable String path, @Nullable List<RequestMethod> methods) {
        this.setPath(path)
            .setMethods(methods)
            .setPathVariableNames()
            .setPathPattern();
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

    public List<String> getPathVariableNames() {
        return pathVariableNames;
    }

    private RequestMappingInfo setPathVariableNames() {
        pathVariableNames = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\{([^/]+?)\\}").matcher(path);
        while (matcher.find()) {
            String pathVariableName = matcher.group(1);
            if (pathVariableNames.contains(pathVariableName))
                throw new DuplicatePathVariableNameException(this, pathVariableName);

            pathVariableNames.add(pathVariableName);
        }

        return this;
    }

    private RequestMappingInfo setPathPattern() {
        pathPattern = Pattern.compile(
            "^" + path.replaceAll("\\{[^/]+?\\}", "([^/]+)") + "$"
        );

        return this;
    }

    public boolean matches(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        return pathPattern.matcher(httpServletRequest.getServletPath()).matches() &&
               methods.contains(
                   RequestMethod.valueOf(httpServletRequest.getMethod())
               );
    }

    public Map<String, String> extractPathVariables(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        Matcher matcher = pathPattern.matcher(httpServletRequest.getServletPath());
        if (!matcher.matches()) return Collections.emptyMap();

        Map<String, String> pathVariables = new HashMap<>();
        for (int i = 0; i < pathVariableNames.size(); i++)
            pathVariables.put(pathVariableNames.get(i), matcher.group(i + 1));

        return pathVariables;
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
