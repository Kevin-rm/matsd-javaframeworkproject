package mg.itu.prom16.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import mg.itu.prom16.base.internal.handler.AbstractHandler;
import mg.itu.prom16.exceptions.InvalidReturnTypeException;
import mg.itu.prom16.support.ThirdPartyConfiguration;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.servletwrapper.exceptions.HttpStatusException;
import mg.matsd.javaframework.servletwrapper.http.*;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

class ResponseRenderer {
    private static final String ERROR_PAGE_TEMPLATE_FILE = "error-pages/exception-page.html";
    private static final String ERROR_PAGE_TEMPLATE_CONTENT;

    static {
        try (
            ClassPathResource classPathResource = new ClassPathResource(ERROR_PAGE_TEMPLATE_FILE);
            BufferedReader bufferedReader       = new BufferedReader(new InputStreamReader(classPathResource.getInputStream(), StandardCharsets.UTF_8))
        ) {
            ERROR_PAGE_TEMPLATE_CONTENT = bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final WebApplicationContainer webApplicationContainer;

    ResponseRenderer(final WebApplicationContainer webApplicationContainer) {
        this.webApplicationContainer = webApplicationContainer;
    }

    void doRenderError(Throwable throwable, Request request, Response response) throws IOException {
        final StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));

        final HttpStatusCode httpStatusCode = throwable instanceof HttpStatusException httpStatusException ?
            HttpStatusCode.valueOf(httpStatusException.getStatusCode()) : HttpStatusCode.INTERNAL_SERVER_ERROR;
        final ObjectMapper objectMapper = (ObjectMapper) webApplicationContainer.getManagedInstance(ThirdPartyConfiguration.JACKSON_OBJECT_MAPPER_ID);
        response.asHtml()
            .setStatus(httpStatusCode.getValue())
            .write(ERROR_PAGE_TEMPLATE_CONTENT.replace("<!-- ERROR_DATA_PLACEHOLDER -->",
                String.format("<script>window.ERROR_DATA = %s;</script>",
                    objectMapper.writeValueAsString(new Error(
                        httpStatusCode.getReason(),
                        new Error.AppDetails(
                            System.getProperty("java.version"),
                            WebApplicationContainer.FRAMEWORK_VERSION,
                            request.getServletContext().getServerInfo(),
                            request.getContextPath()),
                        new Error.RequestInfo(
                            request.getMethod(),
                            request.getFullUrl(),
                            request.getHeaders(),
                            null), // Let's assume this is null for now
                        new Error.Exception(
                            throwable.getClass().getName(),
                            throwable.getMessage(),
                            stringWriter.toString())
                    ))
                ))
            ).flush();
    }

    void doRender(
        Request  request,
        Response response,
        Session  session,
        AbstractHandler handler,
        Object additionalParameter
    ) throws ServletException, IOException {
        Model model = (Model) webApplicationContainer.getManagedInstance(Model.MANAGED_INSTANCE_ID);
        FlashBag flashBag = session.getFlashBag();
        flashBag.peekAll().keySet()
            .stream()
            .filter(k -> k.startsWith(RedirectData.KEY_PREFIX))
            .forEachOrdered(k -> model.addData(k.substring(RedirectData.KEY_PREFIX.length()), flashBag.get(k)));

        Object handlerMethodResult = handler.invokeMethod(webApplicationContainer,
            request, response, session, additionalParameter);
        Method handlerMethod = handler.getMethod();

        model.setAttributes(request);
        if (handler.isJsonResponse())
            handleJsonResult(response, handler.getControllerClass(), handlerMethod, handlerMethodResult);
        else if (handlerMethodResult instanceof ModelAndView modelAndView) {
            modelAndView.getModel().setAttributes(request);
            response.forwardTo(modelAndView.getView());
        } else if (handlerMethodResult instanceof RedirectView redirectView)
            response.redirect(redirectView.buildCompleteUrl());
        else if (handlerMethodResult instanceof String string)
            handleStringResult(request, response, string);
        else throw new InvalidReturnTypeException(handlerMethod);
    }

    private void handleJsonResult(
        Response response,
        Class<?> controllerClass,
        Method   handlerMethod,
        Object   handlerMethodResult
    ) throws IOException {
        if (handlerMethod.getReturnType() == void.class)
            throw new InvalidReturnTypeException(String.format("Impossible d'envoyer une réponse sous le format \"JSON\" lorsque " +
                "le type de retour est \"void\": méthode \"%s\" du contrôleur \"%s\"", handlerMethod.getName(), controllerClass)
            );

        response.asJson();
        ObjectMapper objectMapper = (ObjectMapper) webApplicationContainer.getManagedInstance(ThirdPartyConfiguration.JACKSON_OBJECT_MAPPER_ID);
        objectMapper.writeValue(response.getWriter(),
            handlerMethodResult instanceof ModelAndView modelAndView ? modelAndView.getData() : handlerMethodResult);
    }

    private void handleStringResult(
        Request request, Response response, String originalString
    ) throws ServletException, IOException {
        originalString = originalString.strip();
        String string = "/" + originalString;
        if (!string.endsWith(".jsp")) string += ".jsp";

        if (request.getRaw().getServletContext().getResource(string) != null) {
            response.forwardTo(string);
            return;
        }

        String[] originalStringParts = originalString.split(":", 2);
        if (!originalStringParts[0].stripTrailing().equalsIgnoreCase("redirect")) {
            response.asHtml()
                .print(originalString)
                .flush();
            return;
        }

        response.redirect(
            ((RedirectData) webApplicationContainer.getManagedInstance(RedirectData.MANAGED_INSTANCE_ID))
            .buildCompleteUrl(originalStringParts[1].stripLeading()));
    }

    private record Error(
        String      statusCodeReason,
        AppDetails  appDetails,
        RequestInfo requestInfo,
        Exception   exception
    ) {
        record AppDetails(
            String javaVersion,
            String matsdjavaframeworkVersion,
            String serverInfo,
            String contextPath
        ) { }

        record RequestInfo(
            String method,
            String url,
            Map<String, String> headers,
            Map<String, Object> body
        ) { }

        record Exception(
            String className,
            String message,
            String stackTrace
        ) { }
    }
}
