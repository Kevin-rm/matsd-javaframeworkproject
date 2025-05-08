package mg.matsd.javaframework.security.filters;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.exceptions.ForbiddenException;
import mg.matsd.javaframework.servletwrapper.http.Request;
import mg.matsd.javaframework.servletwrapper.http.Response;

import java.util.UUID;

public class CsrfFilter extends BaseFilter {
    private String sessionKey;
    private String headerName;
    private String parameterName;

    public CsrfFilter() {
        sessionKey    = "csrf_token";
        headerName    = "X-CSRF-Token";
        parameterName = "_csrf";
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public CsrfFilter setSessionKey(String sessionKey) {
        Assert.notBlank(sessionKey, false, "L'argument sessionKey ne peut pas être vide ou \"null\"");

        this.sessionKey = sessionKey.strip();
        return this;
    }

    public String getHeaderName() {
        return headerName;
    }

    public CsrfFilter setHeaderName(String headerName) {
        Assert.notBlank(headerName, false, "L'argument headerName ne peut pas être vide ou \"null\"");

        this.headerName = headerName.strip();
        return this;
    }

    public String getParameterName() {
        return parameterName;
    }

    public CsrfFilter setParameterName(String parameterName) {
        Assert.notBlank(parameterName, false, "L'argument parameterName ne peut pas être vide ou \"null\"");

        this.parameterName = parameterName.strip();
        return this;
    }

    @Override
    public FilterChainDecision preHandle(Request request, Response response)
        throws Exception {

        final String csrfToken = getOrGenerateCsrfToken(request);
        if (isStateChangingRequest(request) && !validateCsrfToken(request, csrfToken))
            throw new ForbiddenException("Csrf token invalide");

        response.setHeader(headerName, csrfToken);
        return FilterChainDecision.defaultValue();
    }

    private String getOrGenerateCsrfToken(Request request) {
        return (String) request.getSession()
            .getOrCreate(sessionKey, UUID.randomUUID().toString());
    }

    private boolean validateCsrfToken(Request request, String csrfToken) {
        String tokenFromRequestHeader = request.getRaw().getHeader(headerName);
        String tokenFromRequestInput  = request.input(parameterName);

        return csrfToken.equals(tokenFromRequestHeader) || csrfToken.equals(tokenFromRequestInput);
    }

    private static boolean isStateChangingRequest(Request request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }
}
