package mg.itu.prom16.base;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import mg.itu.prom16.http.SessionImpl;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.utils.WebUtils;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
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

                mappingHandlerMap.put(requestMappingInfo, new MappingHandler(controllerClass, method));
            }
        }
    }

    @Nullable
    private Map.Entry<RequestMappingInfo, MappingHandler> resolveMappingHandler(HttpServletRequest request) {
        return mappingHandlerMap.entrySet().stream()
            .filter(entry -> entry.getKey().matches(request))
            .findFirst()
            .orElse(null);
    }

    private void stringToHttpResponse(
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
            httpServletResponse.getWriter().print(originalString);
        }

        originalStringParts[1] = originalStringParts[1].stripLeading();
        if (UtilFunctions.isAbsoluteUrl(originalStringParts[1])) {
            httpServletResponse.sendRedirect(originalStringParts[1]);
            return;
        }

        httpServletResponse.sendRedirect(WebUtils.absolutePath(originalStringParts[1]));
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        Session session = ((Session) webApplicationContainer.getManagedInstance(Session.class))
            .setHttpSession(request.getSession());
        RequestContextHolder.setServletRequestAttributes(new ServletRequestAttributes(request, response, session));

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
            Object controllerMethodResult = mappingHandler.invokeMethod(
                webApplicationContainer, request, response, session, mappingHandlerEntry.getKey()
            );
            if (controllerMethodResult instanceof ModelView modelView) {
                modelView.getData().forEach(request::setAttribute);

                request.getRequestDispatcher(modelView.getView()).forward(request, response);
            } else if (controllerMethodResult instanceof RedirectView redirectView)
                response.sendRedirect(redirectView.buildCompleteUrl());
            else if (controllerMethodResult instanceof String string)
                stringToHttpResponse(request, response, string);
            else throw new InvalidReturnTypeException(mappingHandler.getMethod());
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
}
