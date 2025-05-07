package mg.matsd.javaframework.servletwrapper.base;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.servletwrapper.http.Request;
import mg.matsd.javaframework.servletwrapper.http.Response;

import java.io.IOException;

public class ServletWrapper extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request  request  = new Request(req);
        Response response = new Response(resp);
        try {
            handleRequest(request, response);
            if (resp.isCommitted()) return;

            switch (req.getMethod().toUpperCase()) {
                case "GET"     -> get(request, response);
                case "POST"    -> post(request, response);
                case "PUT"     -> put(request, response);
                case "DELETE"  -> delete(request, response);
                case "HEAD"    -> head(request, response);
                case "OPTIONS" -> options(request, response);
                case "TRACE"   -> trace(request, response);
                default -> {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleRequest(Request request, Response response) throws Exception { }

    protected void get(Request request, Response response) throws Exception {

    }

    protected void post(Request request, Response response) throws Exception {

    }

    protected void put(Request request, Response response) throws Exception {

    }

    protected void delete(Request request, Response response) throws Exception {

    }

    protected void head(Request request, Response response) throws Exception {

    }

    protected void options(Request request, Response response) throws Exception {

    }

    protected void trace(Request request, Response response) throws Exception {

    }
}
