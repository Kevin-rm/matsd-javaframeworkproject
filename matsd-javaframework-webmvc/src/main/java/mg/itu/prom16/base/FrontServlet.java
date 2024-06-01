package mg.itu.prom16.base;

import jakarta.servlet.RequestDispatcher;
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
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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

        for (Class<?> controllerClass : UtilFunctions.findControllers(controllerPackage)) {
            String pathPrefix = "";
            List<RequestMethod> sharedRequestMethods = new ArrayList<>();

            if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);

                pathPrefix           = requestMapping.value();
                sharedRequestMethods = Arrays.asList(requestMapping.methods());
            }

            for (Method method : controllerClass.getDeclaredMethods()) {
                if (
                    !AnnotationUtils.hasAnnotation(RequestMapping.class, method) ||
                    method.getModifiers() == Modifier.PRIVATE
                ) continue;

                Map<String, Object> requestMappingInfoAttributes = UtilFunctions.getRequestMappingInfoAttributes(method);
                List<RequestMethod> requestMethods = Arrays.asList(
                    (RequestMethod[]) requestMappingInfoAttributes.get("methods")
                );
                requestMethods.addAll(sharedRequestMethods);

                RequestMappingInfo requestMappingInfo = new RequestMappingInfo(
                    pathPrefix + requestMappingInfoAttributes.get("path"), requestMethods
                );
                if (mappingHandlerMap.containsKey(requestMappingInfo))
                    throw new DuplicateMappingException(requestMappingInfo);

                mappingHandlerMap.put(requestMappingInfo, new MappingHandler(controllerClass, method));
            }
        }

        return this;
    }

    @Nullable
    private MappingHandler resolveMappingHandler(HttpServletRequest request) {
        for (Map.Entry<RequestMappingInfo, MappingHandler> entry : mappingHandlerMap.entrySet())
            if (entry.getKey().matches(request)) return entry.getValue();

        return null;
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter printWriter = response.getWriter();

        MappingHandler mappingHandler = resolveMappingHandler(request);
        if (mappingHandler == null)
            printWriter.write("404 - Not Found");
        else {
            try {
                Object controllerInstance = mappingHandler.getControllerClass().getConstructor().newInstance();

                Object controllerMethodResult = mappingHandler.invokeMethod(controllerInstance);
                if (controllerMethodResult instanceof ModelView modelView) {
                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet())
                        request.setAttribute(entry.getKey(), entry.getValue());

                    RequestDispatcher requestDispatcher = request.getRequestDispatcher(modelView.getView());
                    requestDispatcher.include(request, response);
                } else {
                    response.setContentType("text/html");
                    printWriter.print(controllerMethodResult);
                }
            } catch (IllegalAccessException | InvocationTargetException |
                     InstantiationException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
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
