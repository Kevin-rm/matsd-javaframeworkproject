package mg.itu.prom16.base;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.JsonResponse;
import mg.itu.prom16.annotations.RequestMapping;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.base.internal.handler.ExceptionHandler;
import mg.itu.prom16.base.internal.handler.MappingHandler;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.base.internal.request.ServletRequestAttributes;
import mg.itu.prom16.exceptions.DuplicateMappingException;
import mg.itu.prom16.http.RequestMethod;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FrontServlet extends HttpServlet {
    private WebApplicationContainer webApplicationContainer;
    private ResponseRenderer responseRenderer;
    private Map<RequestMappingInfo, MappingHandler> mappingHandlerMap;
    private List<ExceptionHandler> exceptionHandlers;

    @Override
    public void init() {
        webApplicationContainer = new WebApplicationContainer(
            getServletContext(),
            getServletConfig().getInitParameter("containerConfigLocation")
        );
        responseRenderer = new ResponseRenderer(webApplicationContainer);
        initHandlers();
    }

    private void initHandlers() {
        Assert.state(webApplicationContainer.hasPerformedComponentScan(),
            String.format("Le scan des \"components\" n'a pas été effectué car la balise <container:component-scan> n'a pas été trouvée " +
                "dans le fichier de configuration \"%s\"", webApplicationContainer.getXmlResourceName())
        );

        for (Class<?> controllerClass : webApplicationContainer.retrieveControllerClasses()) {
            String pathPrefix = "";
            List<RequestMethod> sharedRequestMethods = new ArrayList<>();
            boolean jsonResponse = AnnotationUtils.hasAnnotation(JsonResponse.class, controllerClass);

            if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);

                pathPrefix = requestMapping.value();
                sharedRequestMethods = Arrays.asList(requestMapping.methods());
            }

            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.getModifiers() == Modifier.PRIVATE) continue;

                if (AnnotationUtils.hasAnnotation(RequestMapping.class, method)) {
                    RequestMappingInfo requestMappingInfo = new RequestMappingInfo(
                        pathPrefix, UtilFunctions.getRequestMappingInfoAttributes(method), sharedRequestMethods
                    );

                    if (mappingHandlerMap == null) mappingHandlerMap = new HashMap<>();
                    if (mappingHandlerMap.containsKey(requestMappingInfo))
                        throw new DuplicateMappingException(requestMappingInfo);

                    mappingHandlerMap.put(requestMappingInfo,
                        new MappingHandler(controllerClass, method, jsonResponse)
                    );
                } else if (method.isAnnotationPresent(mg.itu.prom16.annotations.ExceptionHandler.class)) {
                    Class<? extends Throwable>[] exceptionClasses = method.getAnnotation(mg.itu.prom16.annotations.ExceptionHandler.class).value();
                    if (exceptionClasses.length == 0) continue;

                    if (exceptionHandlers == null) exceptionHandlers = new ArrayList<>();
                    exceptionHandlers.add(
                        new ExceptionHandler(controllerClass, method, jsonResponse, exceptionClasses, false)
                    );
                }
            }
        }
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        RequestContextHolder.setServletRequestAttributes(new ServletRequestAttributes(request, response));
        Session session = ((Session) webApplicationContainer.getManagedInstance(Session.class))
            .setHttpSession(RequestContextHolder.getServletRequestAttributes().getSession());

        Map.Entry<RequestMappingInfo, MappingHandler> mappingHandlerEntry = resolveMappingHandler(request);
        if (mappingHandlerEntry == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                String.format("Aucun mapping trouvé pour le path : \"%s\" et method : \"%s\"",
                    request.getServletPath(), request.getMethod())
            );
            return;
        }

        MappingHandler mappingHandler = mappingHandlerEntry.getValue();
        Method controllerMethod = mappingHandler.getMethod();
        try {
            responseRenderer.doRender(request, response, mappingHandler, controllerMethod,
                mappingHandler.invokeMethod(
                    webApplicationContainer, request, response, session, mappingHandlerEntry.getKey()
                ));
        } catch (Throwable throwable) {
            List<Throwable> throwableTrace = ExceptionHandler.getThrowableTrace(throwable, null);
            ExceptionHandler exceptionHandler = resolveExceptionHandler(throwableTrace, mappingHandler.getControllerClass());
            if (exceptionHandler == null) throw throwable;

            responseRenderer.doRender(request, response, exceptionHandler, exceptionHandler.getMethod(),
                exceptionHandler.invokeMethod(webApplicationContainer, request, response, session, throwableTrace));
        } finally {
            RequestContextHolder.clear();
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

    @Nullable
    private Map.Entry<RequestMappingInfo, MappingHandler> resolveMappingHandler(HttpServletRequest request) {
        return mappingHandlerMap == null ? null : mappingHandlerMap.entrySet().stream()
            .filter(entry -> entry.getKey().matches(request))
            .findFirst()
            .orElse(null);
    }

    @Nullable
    private ExceptionHandler resolveExceptionHandler(List<Throwable> throwableTrace, Class<?> currentControllerClass) {
        return exceptionHandlers == null ? null : exceptionHandlers.stream()
            .filter(exceptionHandler -> exceptionHandler.canHandle(throwableTrace, currentControllerClass))
            .findFirst()
            .orElse(null);
    }
}
