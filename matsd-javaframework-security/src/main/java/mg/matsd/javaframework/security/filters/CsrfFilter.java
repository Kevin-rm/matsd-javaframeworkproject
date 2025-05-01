package mg.matsd.javaframework.security.filters;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.exceptions.ForbiddenException;

import java.util.UUID;

@WebListener("/*")
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

        this.sessionKey = sessionKey;
        return this;
    }

    public String getHeaderName() {
        return headerName;
    }

    public CsrfFilter setHeaderName(String headerName) {
        Assert.notBlank(headerName, false, "L'argument headerName ne peut pas être vide ou \"null\"");

        this.headerName = headerName;
        return this;
    }

    public String getParameterName() {
        return parameterName;
    }

    public CsrfFilter setParameterName(String parameterName) {
        Assert.notBlank(parameterName, false, "L'argument parameterName ne peut pas être vide ou \"null\"");

        this.parameterName = parameterName;
        return this;
    }

    @Override
    public FilterChainDecision preHandle(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        final String csrfToken = getOrGenerateCsrfToken(request);
        if (isStateChangingRequest(request) && !validateCsrfToken(request, csrfToken))
            throw new ForbiddenException("Csrf token invalide");

        response.setHeader(headerName, csrfToken);
        return FilterChainDecision.defaultValue();
    }

    private String getOrGenerateCsrfToken(HttpServletRequest httpServletRequest) {
        HttpSession httpSession = httpServletRequest.getSession();

        String csrfToken = (String) httpSession.getAttribute(sessionKey);
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            httpSession.setAttribute(sessionKey, csrfToken);
        }

        return csrfToken;
    }

    private boolean validateCsrfToken(HttpServletRequest httpServletRequest, String csrfToken) {
        String tokenFromRequestHeader    = httpServletRequest.getHeader(headerName);
        String tokenFromRequestParameter = httpServletRequest.getParameter(parameterName);

        return csrfToken.equals(tokenFromRequestHeader) || csrfToken.equals(tokenFromRequestParameter);
    }

    private static boolean isStateChangingRequest(HttpServletRequest httpServletRequest) {
        String method = httpServletRequest.getMethod();
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }
}
