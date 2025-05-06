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
        Request request = new Request();
        Response response;

        try {
            switch (req.getMethod().toUpperCase()) {
                case "GET":
                    response = get(request);
                    break;
                case "POST":
                    response = post(request);
                    break;
                case "PUT":
                    response = put(request);
                    break;
                case "DELETE":
                    response = delete(request);
                    break;
                case "HEAD":
                    response = head(request);
                    break;
                case "OPTIONS":
                    response = options(request);
                    break;
                case "TRACE":
                    response = trace(request);
                    break;
                default:
                    return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    @Nullable
    protected Response put(Request request) throws Exception {
        return null;
    }

    @Nullable
    protected Response delete(Request request) throws Exception {
        return null;
    }

    @Nullable
    protected Response head(Request request) throws Exception {
        return null;
    }

    @Nullable
    protected Response options(Request request) throws Exception {
        return null;
    }

    @Nullable
    protected Response trace(Request request) throws Exception {
        return null;
    }
}
