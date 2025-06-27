package mg.matsd.javaframework.security.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.http.base.Request;
import mg.matsd.javaframework.http.base.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseFilter implements Filter {
    protected static final Logger LOGGER = LogManager.getLogger(BaseFilter.class);
    protected final String alreadyFilteredAttrName = getClass().getName() + ".FILTERED";
    protected final List<String> urlPatterns       = new ArrayList<>();
    private   FilterConfig filterConfig;

    public String getAlreadyFilteredAttrName() {
        return alreadyFilteredAttrName;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public List<String> getUrlPatterns() {
        return Collections.unmodifiableList(urlPatterns);
    }

    public BaseFilter addUrlPattern(String urlPattern) {
        Assert.notBlank(urlPattern, false, "L'argument urlPattern ne peut pas être vide ou \"null\"");
        urlPatterns.add(urlPattern);

        return this;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        LOGGER.debug("Initialisation du filtre: \"{}\"", filterConfig.getFilterName());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        if (!(servletRequest  instanceof HttpServletRequest httpServletRequest)   ||
            !(servletResponse instanceof HttpServletResponse httpServletResponse) ||
            httpServletRequest.getAttribute(alreadyFilteredAttrName) != null
        ) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        Request request   = new Request(httpServletRequest);
        Response response = new Response(httpServletResponse, request);
        if (shouldIgnore(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        httpServletRequest.setAttribute(alreadyFilteredAttrName, Boolean.TRUE);
        try {
            FilterChainDecision decision = preHandle(request, response);
            switch (decision) {
                case CONTINUE -> {
                    filterChain.doFilter(servletRequest, servletResponse);
                    postHandle(request, response);
                }
                case SKIP_CHAIN -> postHandle(request, response);
                case STOP -> { }
                default -> throw new IllegalStateException("Valeur de \"FilterChainDecision\" non reconnue: " + decision);
            }
        } catch (Exception e) {
            if (e instanceof ServletException servletException)      throw servletException;
            else if (e instanceof RuntimeException runtimeException) throw runtimeException;

            throw new RuntimeException(e);
        } finally {
            if (httpServletRequest.getAttribute(alreadyFilteredAttrName) != null)
                httpServletRequest.removeAttribute(alreadyFilteredAttrName);
        }
    }

    @Nullable
    public abstract FilterChainDecision preHandle(Request request, Response response)
        throws Exception;

    public void postHandle(Request request, Response response)
        throws Exception { }

    public boolean shouldIgnore(Request request) { return false; }

    public enum FilterChainDecision {
        CONTINUE, SKIP_CHAIN, STOP;

        public static FilterChainDecision defaultValue() { return CONTINUE; }
    }
}
