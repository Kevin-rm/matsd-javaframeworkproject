package mg.itu.prom16.base.internal;

import mg.itu.prom16.exceptions.DuplicatePathVariableNameException;
import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.http.base.Request;
import mg.matsd.javaframework.http.base.RequestMethod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestMappingInfo {
    static final String PATH_VARIABLE_DEFAULT_REQUIREMENT = "[^/]+";

    private String path;
    private String name;
    private List<RequestMethod> methods;
    private Map<String, String> pathVariablesAttributes;
    private Pattern pathPattern;

    public RequestMappingInfo(
        String pathPrefix,
        String namePrefix,
        Map<String, Object> requestMappingInfoAttributes,
        List<RequestMethod> sharedRequestMethods
    ) {
        List<RequestMethod> requestMethods = Arrays.asList(
            (RequestMethod[]) requestMappingInfoAttributes.get("methods")
        );
        requestMethods.addAll(sharedRequestMethods);

        this.setPath(pathPrefix + requestMappingInfoAttributes.get("path"))
            .setName(namePrefix, (String) requestMappingInfoAttributes.get("name"))
            .setMethods(requestMethods)
            .setPathVariablesAttributes()
            .setPathPattern();
    }

    public String getPath() {
        return path;
    }

    private RequestMappingInfo setPath(@Nullable String path) {
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

    public String getName() {
        return name;
    }

    private RequestMappingInfo setName(String namePrefix, @Nullable String name) {
        if (name == null || StringUtils.isBlank(name)) return this;

        this.name = StringUtils.hasText(namePrefix) ? namePrefix + "." + name : name;
        return this;
    }

    public List<RequestMethod> getMethods() {
        return methods;
    }

    private RequestMappingInfo setMethods(@Nullable List<RequestMethod> methods) {
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
                 requirement = pathVariableParts[1];
            else requirement = PATH_VARIABLE_DEFAULT_REQUIREMENT;

            pathVariablesAttributes.put(pathVariableParts[0], requirement);
        }

        return this;
    }

    public Map<String, String> getPathVariablesAttributes() {
        return pathVariablesAttributes;
    }

    private RequestMappingInfo setPathPattern() {
        String regex = "^" + path;
        for (Map.Entry<String, String> entry : pathVariablesAttributes.entrySet())
            regex = regex.replace(
                String.format("{%s%s}", entry.getKey(),
                    entry.getValue().equals(PATH_VARIABLE_DEFAULT_REQUIREMENT) ? "" : ":" + entry.getValue()
                ), "(" + entry.getValue() + ")"
            );
        regex += "/*$";

        pathPattern = Pattern.compile(regex);
        return this;
    }

    public boolean matches(final String servletPath, final RequestMethod requestMethod) {
        Assert.notBlank(servletPath, false, "L'argument servletPath ne peut pas être vide ou \"null\"");
        Assert.notNull(requestMethod, "L'argument requestMethod ne peut pas être \"null\"");

        return pathPattern.matcher(servletPath).matches() &&
               methods.contains(requestMethod);
    }

    public Map<String, String> extractPathVariablesValues(Request request) {
        Assert.notNull(request, "La requête ne peut pas être \"null\"");

        Matcher matcher = pathPattern.matcher(request.getServletPath());
        if (!matcher.matches()) return Collections.emptyMap();

        Map<String, String> pathVariablesValues = new HashMap<>();
        int i = 0;
        for (String key : pathVariablesAttributes.keySet()) {
            pathVariablesValues.put(key, matcher.group(i + 1));
            i++;
        }

        return pathVariablesValues;
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
            ", name='" + name + '\'' +
            ", methods=" + methods +
            '}';
    }
}
