package mg.matsd.javaframework.security.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.core.annotations.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class BaseFilter implements Filter {
    protected static final Logger LOGGER = LogManager.getLogger(BaseFilter.class);
    private FilterConfig filterConfig;

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        LOGGER.debug("Initialisation du filtre: \"{}\"", filterConfig.getFilterName());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest  instanceof HttpServletRequest httpServletRequest)   ||
            !(servletResponse instanceof HttpServletResponse httpServletResponse) ||
            shouldIgnore(httpServletRequest)
        ) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        try {
            FilterChainDecision decision = preHandle(httpServletRequest, httpServletResponse);
            switch (decision) {
                case CONTINUE -> {
                    filterChain.doFilter(servletRequest, servletResponse);
                    postHandle(httpServletRequest, httpServletResponse);
                }
                case SKIP_CHAIN -> postHandle(httpServletRequest, httpServletResponse);
                case STOP -> { }
                default -> throw new IllegalStateException("Valeur de FilterChainDecision non reconnue: " + decision);
            }
        } catch (Exception e) {
            if (e instanceof ServletException servletException) throw servletException;

            Throwable cause = e.getCause();
            throw new RuntimeException(cause != null ? cause : e);
        }
    }

    @Nullable
    public abstract FilterChainDecision preHandle(HttpServletRequest request, HttpServletResponse response)
        throws Exception;

    public void postHandle(HttpServletRequest request, HttpServletResponse response)
        throws Exception { }

    public boolean shouldIgnore(HttpServletRequest request) { return false; }

    public enum FilterChainDecision { CONTINUE, SKIP_CHAIN, STOP }
}
