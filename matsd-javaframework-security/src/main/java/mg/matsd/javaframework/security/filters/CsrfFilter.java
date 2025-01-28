package mg.matsd.javaframework.security.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

public class CsrfFilter implements Filter {
    private static final String CSRF_TOKEN_SESSION_KEY = "csrf_token";
    private static final String CSRF_TOKEN_HEADER      = "X-CSRF-Token";
    private static final String CSRF_TOKEN_PARAM_NAME  = "_csrf";

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
        String csrfToken = (String) httpServletRequest.getSession().getAttribute(CSRF_TOKEN_SESSION_KEY);
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            httpServletRequest.getSession().setAttribute(CSRF_TOKEN_SESSION_KEY, csrfToken);
        }

        return csrfToken;
    }

    private static boolean validateCsrfToken(HttpServletRequest httpServletRequest, String csrfToken) {
        String tokenFromHeader = httpServletRequest.getHeader(CSRF_TOKEN_HEADER);
        String tokenFromParam  = httpServletRequest.getParameter(CSRF_TOKEN_PARAM_NAME);

        return csrfToken.equals(tokenFromHeader) || csrfToken.equals(tokenFromParam);
    }

    private static boolean isStateChangingRequest(HttpServletRequest httpServletRequest) {
        String method = httpServletRequest.getMethod();
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }
}
