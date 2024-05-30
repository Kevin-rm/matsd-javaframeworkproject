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
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
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

        for (Class<?> controllerClass : UtilFunctions.findControllers(controllerPackage)) {
            String pathPrefix = "";
            RequestMethod[] sharedRequestMethods = new RequestMethod[0];

            if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);

                pathPrefix           = requestMapping.value();
                sharedRequestMethods = requestMapping.methods();
            }

            for (Method method : controllerClass.getDeclaredMethods()) {
                if (!AnnotationUtils.hasAnnotation(RequestMapping.class, method)) continue;

                Map<String, Object> requestMappingInfoAttributes = UtilFunctions.getRequestMappingInfoAttributes(method);

                RequestMethod[] requestMethods = (RequestMethod[]) requestMappingInfoAttributes.get("methods");
                RequestMethod[] combinedRequestMethods;

                if (requestMethods.length == 0)            combinedRequestMethods = sharedRequestMethods;
                else if (sharedRequestMethods.length == 0) combinedRequestMethods = requestMethods;
                else {
                    combinedRequestMethods = new RequestMethod[sharedRequestMethods.length + requestMethods.length];
                    System.arraycopy(sharedRequestMethods, 0, combinedRequestMethods, 0, sharedRequestMethods.length);
                    System.arraycopy(requestMethods, 0, combinedRequestMethods, sharedRequestMethods.length, requestMethods.length);
                }

                RequestMappingInfo requestMappingInfo = new RequestMappingInfo(
                    pathPrefix + (String) requestMappingInfoAttributes.get("path"), combinedRequestMethods
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
            if (entry.getKey().matches(request))
                return entry.getValue();

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
                Object obj = mappingHandler.getControllerClass().getConstructor().newInstance();
                printWriter.println(
                    mappingHandler.getMethod().invoke(obj)
                );
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
