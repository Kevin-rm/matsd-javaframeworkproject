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
import mg.itu.prom16.exceptions.NotFoundHttpException;
import mg.itu.prom16.http.RequestMethod;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.utils.WebFacade;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FrontServlet extends HttpServlet {
    private static Throwable throwableOnInit;
    private WebApplicationContainer webApplicationContainer;
    private ResponseRenderer responseRenderer;
    private final Map<RequestMappingInfo, MappingHandler> mappingHandlerMap = new HashMap<>();
    private final List<ExceptionHandler> exceptionHandlers = new ArrayList<>();

    @Override
    public void init() {
        try {
            webApplicationContainer = new WebApplicationContainer(
                getServletContext(),
                getServletConfig().getInitParameter("containerConfigLocation")
            );
            responseRenderer = new ResponseRenderer(webApplicationContainer);
            initHandlers();

            WebFacade.setFrontServlet(this);
        } catch (Throwable throwable) {
            throwableOnInit = throwable;
        }
    }

    private void initHandlers() {
        Assert.state(webApplicationContainer.hasPerformedComponentScan(),
            String.format("Le scan des \"components\" n'a pas été effectué car la balise <container:component-scan> n'a pas été trouvée " +
                "dans le fichier de configuration \"%s\"", webApplicationContainer.getXmlResourceName())
        );

        for (Class<?> controllerClass : webApplicationContainer.retrieveControllerClasses()) {
            String pathPrefix = "";
            String namePrefix = "";
            List<RequestMethod> sharedRequestMethods = new ArrayList<>();
            boolean jsonResponse = AnnotationUtils.hasAnnotation(JsonResponse.class, controllerClass);

            if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);

                pathPrefix = requestMapping.value();
                namePrefix = requestMapping.name();
                sharedRequestMethods = Arrays.asList(requestMapping.methods());
            }

            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.getModifiers() == Modifier.PRIVATE) continue;

                if (AnnotationUtils.hasAnnotation(RequestMapping.class, method)) {
                    RequestMappingInfo requestMappingInfo = new RequestMappingInfo(
                        pathPrefix, namePrefix, UtilFunctions.getRequestMappingInfoAttributes(method), sharedRequestMethods
                    );

                    if (mappingHandlerMap.containsKey(requestMappingInfo))
                        throw new DuplicateMappingException(requestMappingInfo);

                    mappingHandlerMap.put(requestMappingInfo,
                        new MappingHandler(controllerClass, method, jsonResponse)
                    );
                } else if (method.isAnnotationPresent(mg.itu.prom16.annotations.ExceptionHandler.class)) {
                    Class<? extends Throwable>[] exceptionClasses = method.getAnnotation(mg.itu.prom16.annotations.ExceptionHandler.class).value();
                    if (exceptionClasses.length == 0) continue;

                    exceptionHandlers.add(
                        new ExceptionHandler(controllerClass, method, jsonResponse, exceptionClasses, false)
                    );
                }
            }
        }
    }

    public RequestMappingInfo getRequestMappingInfoByName(String name) throws ServletException {
        return mappingHandlerMap.entrySet().stream()
            .filter(entry -> name.equals(entry.getKey().getName()))
            .findFirst().map(Map.Entry::getKey)
            .orElseThrow(() -> new ServletException(String.format("Aucun \"RequestMapping\" trouvé avec le nom : \"%s\"", name)));
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        if (throwableOnInit != null) {
            ResponseRenderer.doRenderError(throwableOnInit, response);
            return;
        }

        RequestContextHolder.setServletRequestAttributes(new ServletRequestAttributes(request, response));
        Session session = ((Session) webApplicationContainer.getManagedInstance(Session.class))
            .setHttpSession(RequestContextHolder.getServletRequestAttributes().getSession());

        MappingHandler mappingHandler = null;
        try {
            Map.Entry<RequestMappingInfo, MappingHandler> mappingHandlerEntry = resolveMappingHandler(request);
            if (mappingHandlerEntry == null)
                throw new NotFoundHttpException(String.format("Aucun mapping trouvé pour le path : \"%s\" et method : \"%s\"",
                    request.getServletPath(), request.getMethod()));

            mappingHandler = mappingHandlerEntry.getValue();
            responseRenderer.doRender(request, response, session, mappingHandler, mappingHandlerEntry.getKey());
        } catch (Throwable throwable) {
            if (mappingHandler == null) {
                ResponseRenderer.doRenderError(throwable, response);
                return;
            }
            List<Throwable> throwableTrace = ExceptionHandler.getThrowableTrace(throwable, null);
            ExceptionHandler exceptionHandler = resolveExceptionHandler(throwableTrace, mappingHandler.getControllerClass());

            if (exceptionHandler == null)
                 ResponseRenderer.doRenderError(throwable, response);
            else responseRenderer.doRender(request, response, session, exceptionHandler, throwableTrace);
        } finally {
            RequestContextHolder.clear();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Nullable
    private Map.Entry<RequestMappingInfo, MappingHandler> resolveMappingHandler(HttpServletRequest request) {
        return mappingHandlerMap.isEmpty() ? null : mappingHandlerMap.entrySet().stream()
            .filter(entry -> entry.getKey().matches(request))
            .findFirst()
            .orElse(null);
    }

    @Nullable
    private ExceptionHandler resolveExceptionHandler(List<Throwable> throwableTrace, Class<?> currentControllerClass) {
        return exceptionHandlers.isEmpty() ? null : exceptionHandlers.stream()
            .filter(exceptionHandler -> exceptionHandler.canHandle(throwableTrace, currentControllerClass))
            .findFirst()
            .orElse(null);
    }
}
