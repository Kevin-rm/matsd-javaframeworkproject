package mg.itu.prom16.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.JsonResponse;
import mg.itu.prom16.annotations.RequestMapping;
import mg.itu.prom16.base.internal.MappingHandler;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.UtilFunctions;
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

    @Override
    public void init() {
        webApplicationContainer = new WebApplicationContainer(
            getServletContext(),
            getServletConfig().getInitParameter("containerConfigLocation")
        );
        setMappingHandlerMap();
    }

    private void setMappingHandlerMap() {
        Assert.state(webApplicationContainer.hasPerformedComponentScan(),
            String.format("Le scan des \"components\" n'a pas été effectué car la balise <container:component-scan> n'a pas été trouvée " +
                "dans le fichier de configuration \"%s\"", webApplicationContainer.getXmlResourceName())
        );

        mappingHandlerMap = new HashMap<>();
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

                mappingHandlerMap.put(requestMappingInfo,
                    new MappingHandler(controllerClass, method, jsonResponse || method.isAnnotationPresent(JsonResponse.class))
                );
            }
        }
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        RequestContextHolder.setServletRequestAttributes(new ServletRequestAttributes(request, response));
        Session session = ((Session) webApplicationContainer.getManagedInstance(Session.class))
            .setHttpSession(RequestContextHolder.getServletRequestAttributes().getSession());

        try {
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
            Object controllerMethodResult = mappingHandler.invokeMethod(
                webApplicationContainer, request, response, session, mappingHandlerEntry.getKey()
            );

            response.setCharacterEncoding("UTF-8");
            if (mappingHandler.isJsonResponse())
                handleJsonResult(response, mappingHandler.getControllerClass(), controllerMethod, controllerMethodResult);
            else if (controllerMethodResult instanceof ModelView modelView) {
                modelView.getData().forEach(request::setAttribute);

                request.getRequestDispatcher(modelView.getView()).forward(request, response);
            } else if (controllerMethodResult instanceof RedirectView redirectView)
                response.sendRedirect(redirectView.buildCompleteUrl());
            else if (controllerMethodResult instanceof String string)
                handleStringResult(request, response, string);
            else throw new InvalidReturnTypeException(controllerMethod);
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

    private void handleJsonResult(
        HttpServletResponse httpServletResponse,
        Class<?> controllerClass,
        Method   controllerMethod,
        Object   controllerMethodResult
    ) throws IOException {
        if (controllerMethodResult instanceof ModelView || controllerMethod.getReturnType() == void.class)
            throw new InvalidReturnTypeException(String.format("Impossible d'envoyer une réponse sous le format \"JSON\" lorsque " +
                "le type de retour est \"ModelView\" ou \"void\": méthode \"%s\" du contrôleur \"%s\"", controllerMethod.getName(), controllerClass)
            );

        httpServletResponse.setContentType("application/json");
        ObjectMapper objectMapper = (ObjectMapper) webApplicationContainer.getManagedInstance(WebApplicationContainer.JACKSON_OBJECT_MAPPER_ID);
        objectMapper.writeValue(httpServletResponse.getWriter(), controllerMethodResult);
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
