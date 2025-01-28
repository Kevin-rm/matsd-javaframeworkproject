package mg.matsd.javaframework.security.base;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.List;

public class SecurityFilterChain implements FilterChain {
    private final List<Filter> filters;
    private int currentFilterIndex;

    SecurityFilterChain(List<Filter> filters) {
        this.filters = filters;
        currentFilterIndex = 0;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        if (currentFilterIndex < filters.size())
            filters.get(++currentFilterIndex).doFilter(servletRequest, servletResponse, this);
    }
}
