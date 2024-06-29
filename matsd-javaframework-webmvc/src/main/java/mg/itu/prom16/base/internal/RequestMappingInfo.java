package mg.itu.prom16.base.internal;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.http.RequestMethod;
import mg.itu.prom16.exceptions.DuplicatePathVariableNameException;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestMappingInfo {
    static final String PATH_VARIABLE_DEFAULT_REQUIREMENT = "[^/]+";

    private String path;
    private List<RequestMethod> methods;
    private Map<String, String> pathVariablesAttributes;
    private Pattern pathPattern;

    public RequestMappingInfo(@Nullable String path, @Nullable List<RequestMethod> methods) {
        this.setPath(path)
            .setMethods(methods)
            .setPathVariablesAttributes()
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

    private RequestMappingInfo setPathVariablesAttributes() {
        pathVariablesAttributes = new HashMap<>();

        Matcher matcher = Pattern.compile("\\{([^/]+?)\\}").matcher(path);
        while (matcher.find()) {
            String[] pathVariableParts = matcher.group(1).split(":", 2);
            if (pathVariablesAttributes.containsKey(pathVariableParts[0]))
                throw new DuplicatePathVariableNameException(this, pathVariableParts[0]);

            String requirement;
            if (pathVariableParts.length == 2 && StringUtils.hasText(pathVariableParts[1]))
                requirement  = pathVariableParts[1];
            else requirement = PATH_VARIABLE_DEFAULT_REQUIREMENT;

            pathVariablesAttributes.put(pathVariableParts[0], requirement);
        }

        return this;
    }

    public Map<String, String> getPathVariablesAttributes() {
        return pathVariablesAttributes;
    }

    private RequestMappingInfo setPathPattern() {
        pathPattern = Pattern.compile(createRegexFromPath(
            path, pathVariablesAttributes
        ));

        return this;
    }

    public boolean matches(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        return pathPattern.matcher(httpServletRequest.getServletPath()).matches() &&
               methods.contains(
                   RequestMethod.valueOf(httpServletRequest.getMethod())
               );
    }

    public Map<String, String> extractPathVariablesValues(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "L'argument httpServletRequest ne peut pas être \"null\"");

        Matcher matcher = pathPattern.matcher(httpServletRequest.getServletPath());
        if (!matcher.matches()) return Collections.emptyMap();

        Map<String, String> pathVariablesValues = new HashMap<>();
        int i = 0;
        for (String key : pathVariablesAttributes.keySet()) {
            pathVariablesValues.put(key, matcher.group(i + 1));
            i++;
        }

        return pathVariablesValues;
    }

    private static String createRegexFromPath(String path, Map<String, String> pathVariablesAttributes) {
        String regex = "^" + path;
        for (Map.Entry<String, String> entry : pathVariablesAttributes.entrySet())
            regex = regex.replace(
                String.format(
                    "{%s%s}",
                    entry.getKey(),
                    entry.getValue().equals(PATH_VARIABLE_DEFAULT_REQUIREMENT) ? "" : ":" + entry.getValue()
                ),
                "(" + entry.getValue() + ")"
            );
        regex += "$";

        return regex;
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
