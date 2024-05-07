package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "frontController", urlPatterns = "/")
public class FrontServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter printWriter = response.getWriter();

        printWriter.write(request.getRequestURI());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }
}
