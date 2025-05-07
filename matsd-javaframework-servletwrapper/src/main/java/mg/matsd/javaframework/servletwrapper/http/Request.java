package mg.matsd.javaframework.servletwrapper.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringToTypeConverter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Request {
    protected final HttpServletRequest raw;
    @Nullable
    protected Session session;
    @Nullable
    private Map<String, String[]> queryParameters;
    @Nullable
    private Map<String, String[]> inputParameters;

    public enum ParameterType { QUERY, INPUT, BOTH }

    public Request(HttpServletRequest raw) {
        Assert.notNull(raw, "L'argument raw ne peut pas être \"null\"");
        this.raw = raw;
    }

    public HttpServletRequest getRaw() {
        return raw;
    }

    public Session getSession() {
        if (session != null) return session;

        HttpSession httpSession = raw.getSession();
        if (httpSession == null) httpSession = raw.getSession(true);

        return session = new Session(httpSession);
    }

    @Nullable
    public <T> T getAttribute(String name, @Nullable T defaultValue, Class<T> expectedType) {
        Assert.notBlank(name, false, "Le nom de l'attribut ne peut pas être vide ou \"null\"");
        Assert.notNull(expectedType, "L'argument expectedType ne peut pas être \"null\"");

        Object attribute = raw.getAttribute(name);
        return attribute == null ? defaultValue : expectedType.cast(attribute);
    }

    @Nullable
    public <T> T getAttribute(String name, Class<T> expectedType) {
        return getAttribute(name, null, expectedType);
    }

    @Nullable
    public Object getAttribute(String name, @Nullable Object defaultValue) {
        return getAttribute(name, defaultValue, Object.class);
    }

    @Nullable
    public Object getAttribute(String name) {
        return getAttribute(name, null);
    }

    public Request setAttribute(String name, @Nullable Object value) {
        Assert.notBlank(name, false, "Le nom de l'attribut ne peut pas être vide ou \"null\"");

        raw.setAttribute(name, value);
        return this;
    }

    public boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }

    public Map<String, String[]> getQueryParameters() {
        if (this.queryParameters != null) return this.queryParameters;

        final String queryString = raw.getQueryString();
        if (StringUtils.isNullOrBlank(queryString)) return Collections.emptyMap();

        Map<String, List<String>> queryParameters = new HashMap<>();
        for (String keyValuePairString : URLDecoder.decode(queryString, StandardCharsets.UTF_8).split("&")) {
            if (keyValuePairString.isEmpty()) continue;

            final String[] keyValuePair = keyValuePairString.split("=", 2);
            queryParameters.computeIfAbsent(keyValuePair[0], __ -> new ArrayList<>())
                .addAll(Arrays.stream(
                    (keyValuePair.length > 1 ? keyValuePair[1] : "").split(",")
                ).filter(csvValue -> !csvValue.isEmpty()).toList());
        }

        this.queryParameters = Collections.unmodifiableMap(queryParameters.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toArray(String[]::new))
            ));
        return this.queryParameters;
    }

    public Map<String, String[]> getInputParameters() {
        if (this.inputParameters != null) return this.inputParameters;

        Map<String, String[]> queryParameters = getQueryParameters();
        Map<String, String[]> inputParameters = new HashMap<>();

        getAllParameters().forEach((key, values) -> {
            if (!queryParameters.containsKey(key)) inputParameters.put(key, values);
        });

        this.inputParameters = Collections.unmodifiableMap(inputParameters);
        return this.inputParameters;
    }

    public Map<String, String[]> getAllParameters() {
        return raw.getParameterMap();
    }

    @Nullable
    public String[] getParameterValues(String name, @Nullable String[] defaultValues) {
        validateParameterName(name);

        String[] parameterValues = raw.getParameterValues(name);
        return parameterValues   == null ? defaultValues : parameterValues;
    }

    @Nullable
    public String[] getParameterValues(String name) {
        return getParameterValues(name, null);
    }

    public boolean hasParameterValues(String name) {
        return getParameterValues(name) != null;
    }

    @Nullable
    public String getParameter(String name, @Nullable String defaultValue) {
        validateParameterName(name);

        String parameter = raw.getParameter(name);
        return parameter == null ? defaultValue : parameter;
    }

    @Nullable
    public String getParameter(String name) {
        return getParameter(name, null);
    }

    @Nullable
    public <T> T getParameterAs(
        String name, @Nullable T defaultValue, Class<T> type, ParameterType parameterType
    ) throws TypeMismatchException {
        Assert.notNull(type, "L'argument type ne peut pas être \"null\"");
        Assert.notNull(parameterType, "L'argument parameterType ne peut pas être \"null\"");

        final String parameter = switch (parameterType) {
            case QUERY -> query(name);
            case INPUT -> input(name);
            case BOTH  -> getParameter(name);
        };

        return parameter == null ? defaultValue : StringToTypeConverter.convert(parameter, type);
    }

    @Nullable
    public Integer getParameterAsInt(String name, @Nullable Integer defaultValue) {
        return getParameterAs(name, defaultValue, Integer.class, ParameterType.BOTH);
    }

    @Nullable
    public Integer getParameterAsInt(String name) {
        return getParameterAsInt(name, null);
    }

    @Nullable
    public Double getParameterAsDouble(String name, @Nullable Double defaultValue) {
        return getParameterAs(name, defaultValue, Double.class, ParameterType.BOTH);
    }

    @Nullable
    public Double getParameterAsDouble(String name) {
        return getParameterAsDouble(name, null);
    }

    @Nullable
    public Float getParameterAsFloat(String name, @Nullable Float defaultValue) {
        return getParameterAs(name, defaultValue, Float.class, ParameterType.BOTH);
    }

    @Nullable
    public Float getParameterAsFloat(String name) {
        return getParameterAsFloat(name, null);
    }

    @Nullable
    public Boolean getParameterAsBoolean(String name, @Nullable Boolean defaultValue) {
        return getParameterAs(name, defaultValue, Boolean.class, ParameterType.BOTH);
    }

    @Nullable
    public Boolean getParameterAsBoolean(String name) {
        return getParameterAsBoolean(name, null);
    }

    public boolean hasParameter(String name) {
        return getParameter(name) != null;
    }

    @Nullable
    public String[] queryValues(String name, @Nullable String[] defaultValues) {
        validateParameterName(name);

        Map<String, String[]> queryParameters = getQueryParameters();
        return queryParameters.getOrDefault(name, defaultValues);
    }

    @Nullable
    public String[] queryValues(String name) {
        return queryValues(name, null);
    }

    @Nullable
    public String query(String name, @Nullable String defaultValue) {
        String[] queryValues = queryValues(name);
        return queryValues == null ? defaultValue : queryValues[0];
    }

    @Nullable
    public String query(String name) {
        return query(name, null);
    }

    @Nullable
    public Integer queryAsInt(String name, @Nullable Integer integer) {
        return getParameterAs(name, integer, Integer.class, ParameterType.QUERY);
    }

    @Nullable
    public Integer queryAsInt(String name) {
        return queryAsInt(name, null);
    }

    @Nullable
    public Double queryAsDouble(String name, @Nullable Double defaultValue) {
        return getParameterAs(name, defaultValue, Double.class, ParameterType.QUERY);
    }

    @Nullable
    public Double queryAsDouble(String name) {
        return queryAsDouble(name, null);
    }

    @Nullable
    public Float queryAsFloat(String name, @Nullable Float defaultValue) {
        return getParameterAs(name, defaultValue, Float.class, ParameterType.QUERY);
    }

    @Nullable
    public Float queryAsFloat(String name) {
        return queryAsFloat(name, null);
    }

    @Nullable
    public Boolean queryAsBoolean(String name, @Nullable Boolean defaultValue) {
        return getParameterAs(name, defaultValue, Boolean.class, ParameterType.QUERY);
    }

    @Nullable
    public Boolean queryAsBoolean(String name) {
        return queryAsBoolean(name, null);
    }

    public boolean hasQuery(String name) {
        validateParameterName(name);
        return getQueryParameters().containsKey(name);
    }

    @Nullable
    public String[] inputValues(String name, @Nullable String[] defaultValues) {
        validateParameterName(name);

        Map<String, String[]> inputParameters = getInputParameters();
        return inputParameters.getOrDefault(name, defaultValues);
    }

    @Nullable
    public String[] inputValues(String name) {
        return inputValues(name, null);
    }

    @Nullable
    public String input(String name, @Nullable String defaultValue) {
        String[] inputValues = inputValues(name);
        return inputValues == null ? defaultValue : inputValues[0];
    }

    @Nullable
    public String input(String name) {
        return input(name, null);
    }

    @Nullable
    public Integer inputAsInt(String name, @Nullable Integer defaultValue) {
        return getParameterAs(name, defaultValue, Integer.class, ParameterType.INPUT);
    }

    @Nullable
    public Integer inputAsInt(String name) {
        return inputAsInt(name, null);
    }

    @Nullable
    public Double inputAsDouble(String name, @Nullable Double defaultValue) {
        return getParameterAs(name, defaultValue, Double.class, ParameterType.INPUT);
    }

    @Nullable
    public Double inputAsDouble(String name) {
        return inputAsDouble(name, null);
    }

    @Nullable
    public Float inputAsFloat(String name, @Nullable Float defaultValue) {
        return getParameterAs(name, defaultValue, Float.class, ParameterType.INPUT);
    }

    @Nullable
    public Float inputAsFloat(String name) {
        return inputAsFloat(name, null);
    }

    @Nullable
    public Boolean inputAsBoolean(String name, @Nullable Boolean defaultValue) {
        return getParameterAs(name, defaultValue, Boolean.class, ParameterType.INPUT);
    }

    @Nullable
    public Boolean inputAsBoolean(String name) {
        return inputAsBoolean(name, null);
    }

    public boolean hasInput(String name) {
        return getInputParameters().containsKey(name);
    }

    public String getMethod() {
        return raw.getMethod();
    }

    public boolean isMethod(String method) {
        Assert.notBlank(method, false, "L'argument method ne peut pas être vide ou \"null\"");

        return getMethod().equals(method.strip().toUpperCase());
    }

    public boolean isGet() {
        return isMethod("GET");
    }

    public boolean isPost() {
        return isMethod("POST");
    }

    public boolean isPut() {
        return isMethod("PUT");
    }

    public boolean isDelete() {
        return isMethod("DELETE");
    }

    private static void validateParameterName(String name) {
        Assert.notBlank(name, false, "Le nom de paramètre ne peut pas être vide ou \"null\"");
    }
}
