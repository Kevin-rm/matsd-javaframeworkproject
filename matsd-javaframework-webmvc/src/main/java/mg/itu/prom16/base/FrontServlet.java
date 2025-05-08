package mg.itu.prom16.base;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import mg.itu.prom16.annotations.JsonResponse;
import mg.itu.prom16.annotations.RequestMapping;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.base.internal.handler.ExceptionHandler;
import mg.itu.prom16.base.internal.handler.MappingHandler;
import mg.itu.prom16.base.internal.request.RequestContext;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.exceptions.DuplicateMappingException;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.utils.AuthFacade;
import mg.itu.prom16.utils.WebFacade;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.security.annotation.Anonymous;
import mg.matsd.javaframework.security.annotation.Authorize;
import mg.matsd.javaframework.security.base.AuthenticationManager;
import mg.matsd.javaframework.security.base.User;
import mg.matsd.javaframework.security.exceptions.AccessDeniedException;
import mg.matsd.javaframework.security.exceptions.NotFoundHttpException;
import mg.matsd.javaframework.servletwrapper.http.Request;
import mg.matsd.javaframework.servletwrapper.http.RequestMethod;
import mg.matsd.javaframework.servletwrapper.http.Response;
import mg.matsd.javaframework.servletwrapper.http.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FrontServlet extends HttpServlet {
    private static final Logger LOGGER = LogManager.getLogger(FrontServlet.class);

    private static Throwable throwableOnInit;
    private WebApplicationContainer webApplicationContainer;
    private ResponseRenderer responseRenderer;
    private final Map<RequestMappingInfo, MappingHandler> mappingHandlerMap = new HashMap<>();
    private final List<ExceptionHandler> exceptionHandlers = new ArrayList<>();

    @Override
    public void init() {
        try {
            LOGGER.info("Démarrage du conteneur des \"ManagedInstance\"");
            webApplicationContainer = new WebApplicationContainer(
                getServletContext(),
                getServletConfig().getInitParameter("containerConfigLocation"));
            responseRenderer = new ResponseRenderer(webApplicationContainer);
            initHandlers();

            WebFacade.setFrontServlet(this);
        } catch (Throwable throwable) {
            throwableOnInit = throwable;
        }
    }

    public WebApplicationContainer getWebApplicationContainer() {
        return webApplicationContainer;
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

            if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);

                pathPrefix = requestMapping.value();
                namePrefix = requestMapping.name();
                sharedRequestMethods = Arrays.asList(requestMapping.methods());
            }

            final boolean jsonResponse = AnnotationUtils.hasAnnotation(JsonResponse.class, controllerClass);
            final boolean isAnonymous  = AnnotationUtils.hasAnnotation(Anonymous.class, controllerClass);
            final String[] sharedAllowedRoles = controllerClass.isAnnotationPresent(Authorize.class) ?
                controllerClass.getAnnotation(Authorize.class).value() : null;

            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.getModifiers() == Modifier.PRIVATE) continue;

                if (AnnotationUtils.hasAnnotation(RequestMapping.class, method)) {
                    RequestMappingInfo requestMappingInfo = new RequestMappingInfo(
                        pathPrefix, namePrefix, UtilFunctions.getRequestMappingInfoAttributes(method), sharedRequestMethods);

                    if (mappingHandlerMap.containsKey(requestMappingInfo))
                        throw new DuplicateMappingException(requestMappingInfo);

                    mappingHandlerMap.put(requestMappingInfo,
                        new MappingHandler(controllerClass, method, jsonResponse, sharedAllowedRoles, isAnonymous));
                } else if (method.isAnnotationPresent(mg.itu.prom16.annotations.ExceptionHandler.class)) {
                    Class<? extends Throwable>[] exceptionClasses = method.getAnnotation(mg.itu.prom16.annotations.ExceptionHandler.class).value();
                    if (exceptionClasses.length == 0) continue;

                    exceptionHandlers.add(
                        new ExceptionHandler(controllerClass, method, jsonResponse, exceptionClasses, false));
                }
            }
        }
    }

    public RequestMappingInfo getRequestMappingInfoByName(String name) throws JspException {
        return mappingHandlerMap.entrySet().stream()
            .filter(entry -> name.equals(entry.getKey().getName()))
            .findFirst().map(Map.Entry::getKey)
            .orElseThrow(() -> new JspException(String.format("Aucun \"RequestMapping\" trouvé avec le nom : \"%s\"", name)));
    }

    protected final void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws ServletException, IOException {
        final Request  request  = new Request (httpServletRequest);
        final Response response = new Response(httpServletResponse, request)
            .setCharset("UTF-8");

        if (throwableOnInit != null) {
            ResponseRenderer.doRenderError(throwableOnInit, response);
            LOGGER.fatal("Une erreur s'est produite durant l'initialisation du \"FrontServlet\"", throwableOnInit);
            return;
        }

        RequestContextHolder.setRequestContext(new RequestContext(request, response));
        final Session session = request.getSession();

        MappingHandler mappingHandler = null;
        try {
            final String servletPath   = request.getServletPath();
            final String requestMethod = request.getMethod();

            Map.Entry<RequestMappingInfo, MappingHandler> mappingHandlerEntry = resolveMappingHandler(servletPath, RequestMethod.valueOf(requestMethod));
            if (mappingHandlerEntry == null)
                throw new NotFoundHttpException(String.format("Aucun mapping trouvé pour le path : \"%s\" et method : \"%s\"",
                    servletPath, requestMethod));
            mappingHandler = mappingHandlerEntry.getValue();

            AuthenticationManager authenticationManager = AuthFacade.getAuthenticationManager();
            if (authenticationManager != null) {
                final User currentUser = authenticationManager.getCurrentUser();
                final boolean isUserAuthenticated = currentUser != null;
                final String statefulStorageKey   = authenticationManager.getStatefulStorageKey();

                if (isUserAuthenticated && statefulStorageKey != null)
                    session.set(statefulStorageKey, authenticationManager.getUserProvider().refreshUser(currentUser));
                if (mappingHandler.isAnonymous() && isUserAuthenticated)
                    throw new AccessDeniedException(String.format("Vous devez être anonyme " +
                        "pour accéder à la ressource \"%s\"", servletPath), servletPath);

                final List<String> allowedRoles = mappingHandler.getAllowedRoles();
                if (allowedRoles != null) {
                    if (!isUserAuthenticated) throw new AccessDeniedException(String.format("Vous devez être authentifié " +
                        "pour accéder à la ressource \"%s\"", servletPath), servletPath);
                    if (!allowedRoles.isEmpty() && allowedRoles.stream().noneMatch(currentUser::hasRole))
                        throw new AccessDeniedException(servletPath, allowedRoles);
                }
            }

            responseRenderer.doRender(request, response, session, mappingHandler, mappingHandlerEntry.getKey());
        } catch (Throwable throwable) {
            List<Throwable> throwableTrace = ExceptionHandler.getThrowableTrace(throwable, null);
            ExceptionHandler exceptionHandler = resolveExceptionHandler(throwableTrace,
                mappingHandler == null ? null : mappingHandler.getControllerClass());

            if (exceptionHandler == null) {
                ResponseRenderer.doRenderError(throwable, response);
                LOGGER.error("", throwable);
            } else responseRenderer.doRender(request, response, session, exceptionHandler, throwableTrace);
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
    private Map.Entry<RequestMappingInfo, MappingHandler> resolveMappingHandler(
        final String servletPath, final RequestMethod requestMethod
    ) {
        return mappingHandlerMap.isEmpty() ? null : mappingHandlerMap.entrySet().stream()
            .filter(entry -> entry.getKey().matches(servletPath, requestMethod))
            .min((entry1, entry2) -> {
                boolean isStatic1  = entry1.getKey().getPath().equals(servletPath);
                boolean isStatic2  = entry2.getKey().getPath().equals(servletPath);

                return isStatic1 && !isStatic2 ? -1 : isStatic2 && !isStatic1 ? 1 : 0;
            })
            .orElse(null);
    }

    @Nullable
    private ExceptionHandler resolveExceptionHandler(
        List<Throwable> throwableTrace, @Nullable Class<?> currentControllerClass
    ) {
        return exceptionHandlers.isEmpty() ? null : exceptionHandlers.stream()
            .filter(exceptionHandler -> exceptionHandler.canHandle(throwableTrace, currentControllerClass))
            .findFirst()
            .orElse(null);
    }
}
