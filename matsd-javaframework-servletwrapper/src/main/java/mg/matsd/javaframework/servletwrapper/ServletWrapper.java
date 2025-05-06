package mg.matsd.javaframework.servletwrapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.servletwrapper.http.Request;
import mg.matsd.javaframework.servletwrapper.http.Response;

import java.io.IOException;

public class ServletWrapper extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request;
        Response response;

        switch (req.getMethod().toUpperCase()) {
            case "GET":
                break;
            case "POST":
                break;
            default:
                break;
        }
    }

    @Nullable
    protected Response get(Request request) throws Exception {
        return null;
    }

    @Nullable
    protected Response post(Request request) throws Exception {
        return null;
    }
}
