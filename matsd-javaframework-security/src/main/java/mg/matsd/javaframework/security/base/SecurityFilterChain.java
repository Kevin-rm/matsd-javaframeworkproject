package mg.matsd.javaframework.security.base;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.List;

public class SecurityFilterChain implements FilterChain {
    private final List<Filter> filters;

    SecurityFilterChain(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {

    }
}
