package mg.matsd.javaframework.security.filters;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class OncePerRequestFilter extends BaseFilter {
    protected final String alreadyFilteredAttrName = defineAlreadyFilteredAttrName();

    public String getAlreadyFilteredAttrName() {
        return alreadyFilteredAttrName;
    }

    @Override
    public FilterChainDecision preHandle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (request.getAttribute(alreadyFilteredAttrName) != null) return FilterChainDecision.CONTINUE;

        request.setAttribute(alreadyFilteredAttrName, Boolean.TRUE);
        try {
            return doPreHandle(request, response);
        } catch (Exception e) {
            request.removeAttribute(alreadyFilteredAttrName);
            throw e;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            doPostHandle(request, response);
        } finally {
            if (request.getAttribute(alreadyFilteredAttrName) != null)
                request.removeAttribute(alreadyFilteredAttrName);
        }
    }

    protected abstract FilterChainDecision doPreHandle(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException;

    protected abstract void doPostHandle(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException;

    protected abstract String defineAlreadyFilteredAttrName();
}
