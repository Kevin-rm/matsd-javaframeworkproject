package mg.matsd.javaframework.security.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Middleware {
    boolean onRequestStart(HttpServletRequest request, HttpServletResponse response);

    default void onRequestEnd(HttpServletRequest request, HttpServletResponse response)   { }

    default void onResponseSent(HttpServletRequest request, HttpServletResponse response) { }
}
