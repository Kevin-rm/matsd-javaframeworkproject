package mg.matsd.javaframework.security.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

public class CsrfFilter implements Filter {
    private static final String CSRF_TOKEN_SESSION_KEY    = "csrf_token";
    private static final String CSRF_TOKEN_HEADER         = "X-CSRF-Token";
    private static final String CSRF_TOKEN_PARAMETER_NAME = "_csrf";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest  instanceof HttpServletRequest httpServletRequest) ||
            !(servletResponse instanceof HttpServletResponse httpServletResponse)
        ) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String csrfToken = getOrGenerateCsrfToken(httpServletRequest);
        if (isStateChangingRequest(httpServletRequest) && !validateCsrfToken(httpServletRequest, csrfToken)) {
            // TODO: Maybe throw an exception

            return;
        }
        httpServletResponse.setHeader(CSRF_TOKEN_HEADER, csrfToken);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String getOrGenerateCsrfToken(HttpServletRequest httpServletRequest) {
        HttpSession httpSession = httpServletRequest.getSession();

        String csrfToken = (String) httpSession.getAttribute(CSRF_TOKEN_SESSION_KEY);
        if (csrfToken == null) httpSession.setAttribute(CSRF_TOKEN_SESSION_KEY, UUID.randomUUID().toString());

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
