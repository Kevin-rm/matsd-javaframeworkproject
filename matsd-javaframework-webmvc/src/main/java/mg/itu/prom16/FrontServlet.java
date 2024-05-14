package mg.itu.prom16;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FrontServlet extends HttpServlet {
    private String         controllerPackage;
    private List<Class<?>> controllersClasses;

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();

        this.setControllerPackage(servletContext.getInitParameter("controller-package"))
            .setControllersClasses();
    }

    private FrontServlet setControllerPackage(String controllerPackage) {
        Assert.notBlank(controllerPackage, false,
            "Le nom de package des contrôleurs à scanner ne peut pas être vide ou \"null\"");

        this.controllerPackage = controllerPackage.strip();
        return this;
    }

    private FrontServlet setControllersClasses() {
        controllersClasses = UtilFunctions.findControllers(controllerPackage);
        return this;
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter printWriter = response.getWriter();
        printWriter.write("Voici la liste des contrôleurs : \n");

        for (Class<?> c : controllersClasses) {
            printWriter.write("- " + c.getName() + "\n");
        }
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
