package mg.itu.prom16.base;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.RequestMapping;
import mg.itu.prom16.base.internal.MappingHandler;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.exceptions.DuplicateMappingException;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FrontServlet extends HttpServlet {
    private String controllerPackage;
    private Map<RequestMappingInfo, MappingHandler> mappingHandlerMap;

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();

        this.setControllerPackage(servletContext.getInitParameter("controller-package"))
            .setMappingHandlerMap();
    }

    private FrontServlet setControllerPackage(String controllerPackage) {
        Assert.notBlank(controllerPackage, false,
            "Le nom de package des contrôleurs à scanner ne peut pas être vide ou \"null\"");

        this.controllerPackage = controllerPackage.strip();
        return this;
    }

    private FrontServlet setMappingHandlerMap() {
        mappingHandlerMap = new HashMap<>();
        for (Class<?> controllerClass : UtilFunctions.findControllers(controllerPackage))
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (!UtilFunctions.isAnnotatedWithRequestMappingAndItsShortcuts(method))
                    continue;

                RequestMapping requestMapping = UtilFunctions.requestMapping(method);

                RequestMappingInfo requestMappingInfo = new RequestMappingInfo(requestMapping.path(), requestMapping.methods());
                if (mappingHandlerMap.containsKey(requestMappingInfo))
                    throw new DuplicateMappingException(requestMappingInfo);

                mappingHandlerMap.put(requestMappingInfo, new MappingHandler(controllerClass, method));
            }

        return this;
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter printWriter = response.getWriter();

        for (Map.Entry<RequestMappingInfo, MappingHandler> entry : mappingHandlerMap.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            MappingHandler mappingHandler = entry.getValue();

            if (requestMappingInfo.matches(request)) {
                printWriter.write("Request mapping : " + requestMappingInfo + "; " + mappingHandler);
                return;
            }
        }

        printWriter.write("404 - Not Found");
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
