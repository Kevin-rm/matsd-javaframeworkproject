package mg.matsd.javaframework.security.base;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.List;

public class SecurityFilterChain implements FilterChain {
    private final List<Filter> filters;
    private int currentFilterIndex = 0;

    SecurityFilterChain(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        if (currentFilterIndex < filters.size())
            filters.get(++currentFilterIndex).doFilter(servletRequest, servletResponse, this);
    }
}
