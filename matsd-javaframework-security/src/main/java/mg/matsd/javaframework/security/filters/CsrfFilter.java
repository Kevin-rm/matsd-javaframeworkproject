package mg.matsd.javaframework.security.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.matsd.javaframework.security.exceptions.ForbiddenException;

import java.util.UUID;

public class CsrfFilter extends OncePerRequestFilter {
    public static final String CSRF_TOKEN_SESSION_KEY    = "csrf_token";
    public static final String CSRF_TOKEN_HEADER         = "X-CSRF-Token";
    public static final String CSRF_TOKEN_PARAMETER_NAME = "_csrf";

    @Override
    protected FilterChainDecision doPreHandle(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        final String csrfToken = getOrGenerateCsrfToken(request);
        if (isStateChangingRequest(request) && !validateCsrfToken(request, csrfToken))
            throw new ForbiddenException("Csrf token invalide");

        response.setHeader(CSRF_TOKEN_HEADER, csrfToken);
        return FilterChainDecision.CONTINUE;
    }

    private String getOrGenerateCsrfToken(HttpServletRequest httpServletRequest) {
        HttpSession httpSession = httpServletRequest.getSession();

        String csrfToken = (String) httpSession.getAttribute(CSRF_TOKEN_SESSION_KEY);
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            httpSession.setAttribute(CSRF_TOKEN_SESSION_KEY, csrfToken);
        }

        return csrfToken;
    }

    private static boolean validateCsrfToken(HttpServletRequest httpServletRequest, String csrfToken) {
        String tokenFromRequestHeader    = httpServletRequest.getHeader(CSRF_TOKEN_HEADER);
        String tokenFromRequestParameter = httpServletRequest.getParameter(CSRF_TOKEN_PARAMETER_NAME);

        return csrfToken.equals(tokenFromRequestHeader) || csrfToken.equals(tokenFromRequestParameter);
    }

    private static boolean isStateChangingRequest(HttpServletRequest httpServletRequest) {
        String method = httpServletRequest.getMethod();
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }
}
