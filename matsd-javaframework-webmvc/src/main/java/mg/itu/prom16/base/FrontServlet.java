package mg.itu.prom16.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.JsonResponse;
import mg.itu.prom16.annotations.RequestMapping;
import mg.itu.prom16.base.internal.handler.AbstractHandler;
import mg.itu.prom16.base.internal.handler.MappingHandler;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.base.internal.handler.ExceptionHandler;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.base.internal.request.ServletRequestAttributes;
import mg.itu.prom16.exceptions.DuplicateMappingException;
import mg.itu.prom16.exceptions.InvalidReturnTypeException;
import mg.itu.prom16.http.RequestMethod;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.utils.WebUtils;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FrontServlet extends HttpServlet {
    private WebApplicationContainer webApplicationContainer;
    private Map<RequestMappingInfo, MappingHandler> mappingHandlerMap;
    private List<ExceptionHandler> exceptionHandlers;

    @Override
    public void init() {
        webApplicationContainer = new WebApplicationContainer(
            getServletContext(),
            getServletConfig().getInitParameter("containerConfigLocation")
        );
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
            handleResult(request, response, mappingHandler, controllerMethod,
                mappingHandler.invokeMethod(
                    webApplicationContainer, request, response, session, mappingHandlerEntry.getKey()
                ));
        } catch (Throwable throwable) {
            ExceptionHandler exceptionHandler = resolveExceptionHandler(throwable, mappingHandler.getControllerClass());
            if (exceptionHandler == null) throw throwable;

            handleResult(request, response, exceptionHandler, exceptionHandler.getMethod(),
                exceptionHandler.invokeMethod(
                    webApplicationContainer, request, response, session, ExceptionHandler.getThrowableTrace(throwable, null)
                ));
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
        return mappingHandlerMap.entrySet().stream()
            .filter(entry -> entry.getKey().matches(request))
            .findFirst()
            .orElse(null);
    }

    @Nullable
    private ExceptionHandler resolveExceptionHandler(Throwable throwable, Class<?> currentControllerClass) {
        return exceptionHandlers.stream()
            .filter(exceptionHandler -> exceptionHandler.canHandle(throwable, currentControllerClass))
            .findFirst()
            .orElse(null);
    }

    private void handleResult(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        AbstractHandler handler,
        Method controllerMethod,
        Object controllerMethodResult
    ) throws ServletException, IOException {
        httpServletResponse.setCharacterEncoding("UTF-8");
        if (handler.isJsonResponse())
            handleJsonResult(httpServletResponse, handler.getControllerClass(), controllerMethod, controllerMethodResult);
        else if (controllerMethodResult instanceof ModelAndView modelAndView) {
            modelAndView.getData().forEach(httpServletRequest::setAttribute);

            httpServletRequest.getRequestDispatcher(modelAndView.getView()).forward(httpServletRequest, httpServletResponse);
        } else if (controllerMethodResult instanceof RedirectView redirectView)
            httpServletResponse.sendRedirect(redirectView.buildCompleteUrl());
        else if (controllerMethodResult instanceof String string)
            handleStringResult(httpServletRequest, httpServletResponse, string);
        else throw new InvalidReturnTypeException(controllerMethod);
    }

    private void handleJsonResult(
        HttpServletResponse httpServletResponse,
        Class<?> controllerClass,
        Method   controllerMethod,
        Object   controllerMethodResult
    ) throws IOException {
        if (controllerMethod.getReturnType() == void.class)
            throw new InvalidReturnTypeException(String.format("Impossible d'envoyer une réponse sous le format \"JSON\" lorsque " +
                "le type de retour est \"void\": méthode \"%s\" du contrôleur \"%s\"", controllerMethod.getName(), controllerClass)
            );

        httpServletResponse.setContentType("application/json");
        ObjectMapper objectMapper = (ObjectMapper) webApplicationContainer.getManagedInstance(WebApplicationContainer.JACKSON_OBJECT_MAPPER_ID);
        objectMapper.writeValue(httpServletResponse.getWriter(),
            controllerMethodResult instanceof ModelAndView modelAndView ? modelAndView.getData() : controllerMethodResult);
    }

    private void handleStringResult(
        HttpServletRequest  httpServletRequest,
        HttpServletResponse httpServletResponse,
        String originalString
    ) throws ServletException, IOException {
        originalString = originalString.strip();
        String string = "/" + originalString;
        if (!string.endsWith(".jsp")) string += ".jsp";

        if (getServletContext().getResource(string) != null) {
            httpServletRequest.getRequestDispatcher(string).forward(httpServletRequest, httpServletResponse);
            return;
        }

        String[] originalStringParts = originalString.split(":", 2);
        if (!originalStringParts[0].stripTrailing().equalsIgnoreCase("redirect")) {
            httpServletResponse.setContentType("text/html");

            PrintWriter printWriter = httpServletResponse.getWriter();
            printWriter.print(originalString);
            printWriter.flush();
            return;
        }

        originalStringParts[1] = originalStringParts[1].stripLeading();
        if (UtilFunctions.isAbsoluteUrl(originalStringParts[1])) {
            httpServletResponse.sendRedirect(originalStringParts[1]);
            return;
        }

        httpServletResponse.sendRedirect(WebUtils.absolutePath(originalStringParts[1]));
    }
}
