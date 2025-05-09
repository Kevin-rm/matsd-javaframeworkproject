package mg.matsd.javaframework.servletwrapper.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.CollectionUtils;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringToTypeConverter;
import mg.matsd.javaframework.servletwrapper.base.internal.UtilFunctions;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Request {
    protected final HttpServletRequest raw;
    @Nullable
    protected Session session;
    @Nullable
    private Map<String, Object>   attributes;
    @Nullable
    private Map<String, Cookie>   cookies;
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

    public String getContentType() {
        return raw.getContentType();
    }

    @Nullable
    public Session getSession(boolean createIfNoSession) {
        if (session != null) return session;

        HttpSession httpSession = raw.getSession(createIfNoSession);
        return httpSession == null ? null : (session = new Session(httpSession));
    }

    @Nullable
    public Session getSession() {
        return getSession(false);
    }

    public Map<String, Cookie> getCookies() {
        if (this.cookies != null) return this.cookies;

        Cookie[] cookies = raw.getCookies();
        if (CollectionUtils.isEmpty(cookies)) return Collections.emptyMap();

        this.cookies = Collections.unmodifiableMap(
            Arrays.stream(cookies).collect(
                Collectors.toMap(Cookie::getName, cookie -> cookie, (a, b) -> b)
            ));
        return this.cookies;
    }

    @Nullable
    public Cookie getCookie(String name) {
        Assert.notBlank(name, false, "Le nom du cookie ne peut pas être vide ou \"null\"");

        Map<String, Cookie> cookies = getCookies();
        if (cookies.isEmpty()) return null;

        return cookies.get(name);
    }

    public boolean hasCookie(String name) {
        Assert.notBlank(name, false, "Le nom du cookie ne peut pas être vide ou \"null\"");

        Map<String, Cookie> cookies = getCookies();
        return !cookies.isEmpty() && cookies.containsKey(name);
    }

    public Map<String, Object> getAttributes() {
        if (attributes != null) return attributes;

        attributes = UtilFunctions.collectAttributes(raw.getAttributeNames(), raw::getAttribute);
        return attributes;
    }

    @Nullable
    public <T> T attribute(String name, @Nullable T defaultValue, Class<T> expectedType) {
        Assert.notBlank(name, false, "Le nom de l'attribut ne peut pas être vide ou \"null\"");
        Assert.notNull(expectedType, "L'argument expectedType ne peut pas être \"null\"");

        Object attribute = raw.getAttribute(name);
        return attribute == null ? defaultValue : expectedType.cast(attribute);
    }

    @Nullable
    public <T> T attribute(String name, Class<T> expectedType) {
        return attribute(name, null, expectedType);
    }

    @Nullable
    public Object attribute(String name, @Nullable Object defaultValue) {
        return attribute(name, defaultValue, Object.class);
    }

    @Nullable
    public Object attribute(String name) {
        return attribute(name, (Object) null);
    }

    public Request setAttribute(String name, @Nullable Object value) {
        Assert.notBlank(name, false, "Le nom de l'attribut ne peut pas être vide ou \"null\"");

        raw.setAttribute(name, value);

        if (attributes == null) attributes = new HashMap<>();
        if (value == null) attributes.remove(name);
        else attributes.put(name, value);

        return this;
    }

    public boolean hasAttribute(String name) {
        return attribute(name) != null;
    }

    public Request removeAttribute(String name) {
        Assert.notBlank(name, false, "Le nom de l'attribut ne peut pas être vide ou \"null\"");

        raw.removeAttribute(name);
        if (attributes != null) attributes.remove(name);

        return this;
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
    public String[] getValues(String name, @Nullable String[] defaultValues) {
        validateParameterName(name);

        String[] parameterValues = raw.getParameterValues(name);
        return parameterValues   == null ? defaultValues : parameterValues;
    }

    @Nullable
    public String[] getValues(String name) {
        return getValues(name, null);
    }

    public boolean hasValues(String name) {
        return getValues(name) != null;
    }

    @Nullable
    public String get(String name, @Nullable String defaultValue) {
        validateParameterName(name);

        String parameter = raw.getParameter(name);
        return parameter == null ? defaultValue : parameter;
    }

    @Nullable
    public String get(String name) {
        return get(name, null);
    }

    @Nullable
    public <T> T getAs(
        String name, @Nullable T defaultValue, Class<T> type, ParameterType parameterType
    ) throws TypeMismatchException {
        Assert.notNull(type, "L'argument type ne peut pas être \"null\"");
        Assert.notNull(parameterType, "L'argument parameterType ne peut pas être \"null\"");

        final String parameter = switch (parameterType) {
            case QUERY -> query(name);
            case INPUT -> input(name);
            case BOTH  -> get(name);
        };

        return parameter == null ? defaultValue : StringToTypeConverter.convert(parameter, type);
    }

    @Nullable
    public Integer getAsInt(String name, @Nullable Integer defaultValue) {
        return getAs(name, defaultValue, Integer.class, ParameterType.BOTH);
    }

    @Nullable
    public Integer getAsInt(String name) {
        return getAsInt(name, null);
    }

    @Nullable
    public Double getAsDouble(String name, @Nullable Double defaultValue) {
        return getAs(name, defaultValue, Double.class, ParameterType.BOTH);
    }

    @Nullable
    public Double getAsDouble(String name) {
        return getAsDouble(name, null);
    }

    @Nullable
    public Float getAsFloat(String name, @Nullable Float defaultValue) {
        return getAs(name, defaultValue, Float.class, ParameterType.BOTH);
    }

    @Nullable
    public Float getAsFloat(String name) {
        return getAsFloat(name, null);
    }

    @Nullable
    public Boolean getAsBoolean(String name, @Nullable Boolean defaultValue) {
        return getAs(name, defaultValue, Boolean.class, ParameterType.BOTH);
    }

    @Nullable
    public Boolean getAsBoolean(String name) {
        return getAsBoolean(name, null);
    }

    public boolean has(String name) {
        return get(name) != null;
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
        return getAs(name, integer, Integer.class, ParameterType.QUERY);
    }

    @Nullable
    public Integer queryAsInt(String name) {
        return queryAsInt(name, null);
    }

    @Nullable
    public Double queryAsDouble(String name, @Nullable Double defaultValue) {
        return getAs(name, defaultValue, Double.class, ParameterType.QUERY);
    }

    @Nullable
    public Double queryAsDouble(String name) {
        return queryAsDouble(name, null);
    }

    @Nullable
    public Float queryAsFloat(String name, @Nullable Float defaultValue) {
        return getAs(name, defaultValue, Float.class, ParameterType.QUERY);
    }

    @Nullable
    public Float queryAsFloat(String name) {
        return queryAsFloat(name, null);
    }

    @Nullable
    public Boolean queryAsBoolean(String name, @Nullable Boolean defaultValue) {
        return getAs(name, defaultValue, Boolean.class, ParameterType.QUERY);
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
        return getAs(name, defaultValue, Integer.class, ParameterType.INPUT);
    }

    @Nullable
    public Integer inputAsInt(String name) {
        return inputAsInt(name, null);
    }

    @Nullable
    public Double inputAsDouble(String name, @Nullable Double defaultValue) {
        return getAs(name, defaultValue, Double.class, ParameterType.INPUT);
    }

    @Nullable
    public Double inputAsDouble(String name) {
        return inputAsDouble(name, null);
    }

    @Nullable
    public Float inputAsFloat(String name, @Nullable Float defaultValue) {
        return getAs(name, defaultValue, Float.class, ParameterType.INPUT);
    }

    @Nullable
    public Float inputAsFloat(String name) {
        return inputAsFloat(name, null);
    }

    @Nullable
    public Boolean inputAsBoolean(String name, @Nullable Boolean defaultValue) {
        return getAs(name, defaultValue, Boolean.class, ParameterType.INPUT);
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

    public RequestMethod getMethodAsEnum() {
        return RequestMethod.valueOf(getMethod());
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

    public String getUrlWithoutQueryString() {
        return raw.getRequestURL().toString();
    }

    public String getUri() {
        return raw.getRequestURI();
    }

    public String getContextPath() {
        return raw.getContextPath();
    }

    public String getServletPath() {
        return raw.getServletPath();
    }

    public String getProtocol() {
        return raw.getScheme();
    }

    public String getHttpVersion() {
        return raw.getProtocol();
    }

    public String getServerName() {
        return raw.getServerName();
    }

    public int getPort() {
        return raw.getServerPort();
    }

    private static void validateParameterName(String name) {
        Assert.notBlank(name, false, "Le nom de paramètre ne peut pas être vide ou \"null\"");
    }
}
