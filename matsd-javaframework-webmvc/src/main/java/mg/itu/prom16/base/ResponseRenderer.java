package mg.itu.prom16.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.base.internal.handler.AbstractHandler;
import mg.itu.prom16.exceptions.InvalidReturnTypeException;
import mg.itu.prom16.exceptions.NotFoundHttpException;
import mg.itu.prom16.http.FlashBag;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.support.ThirdPartyConfiguration;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

class ResponseRenderer {
    private static final String ERROR_PAGE_TEMPLATE_FILE = "error-page-template.txt";
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

    static void doRenderError(Throwable throwable, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("text/html");
        httpServletResponse.setStatus(throwable instanceof NotFoundHttpException ?
            NotFoundHttpException.statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));

        PrintWriter printWriter = httpServletResponse.getWriter();
        printWriter.write(String.format(ERROR_PAGE_TEMPLATE_CONTENT,
            throwable.getClass().getName(),
            StringUtils.escapeHtml(throwable.getMessage()),
            httpServletResponse.getStatus(),
            StringUtils.escapeHtml(stringWriter.toString())
        ));
        printWriter.flush();
    }

    void doRender(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Session session,
        AbstractHandler handler,
        Object additionalParameter
    ) throws ServletException, IOException {
        Model model = (Model) webApplicationContainer.getManagedInstance(Model.MANAGED_INSTANCE_ID);
        FlashBag flashBag = session.getFlashBag();
        flashBag.peekAll().keySet()
            .stream()
            .filter(k -> k.startsWith(RedirectData.KEY_PREFIX))
            .forEachOrdered(k -> model.addData(k.substring(RedirectData.KEY_PREFIX.length()), flashBag.get(k)));

        Object handlerMethodResult = handler.invokeMethod(
            webApplicationContainer, httpServletRequest, httpServletResponse, session, additionalParameter);
        Method handlerMethod = handler.getMethod();

        model.setAttributes(httpServletRequest);
        if (handler.isJsonResponse())
            handleJsonResult(httpServletResponse, handler.getControllerClass(), handlerMethod, handlerMethodResult);
        else if (handlerMethodResult instanceof ModelAndView modelAndView) {
            modelAndView.getModel().setAttributes(httpServletRequest);

            httpServletRequest.getRequestDispatcher(modelAndView.getView()).forward(httpServletRequest, httpServletResponse);
        } else if (handlerMethodResult instanceof RedirectView redirectView)
            httpServletResponse.sendRedirect(redirectView.buildCompleteUrl());
        else if (handlerMethodResult instanceof String string)
            handleStringResult(httpServletRequest, httpServletResponse, string);
        else throw new InvalidReturnTypeException(handlerMethod);
    }

    private void handleJsonResult(
        HttpServletResponse httpServletResponse,
        Class<?> controllerClass,
        Method   handlerMethod,
        Object   handlerMethodResult
    ) throws IOException {
        if (handlerMethod.getReturnType() == void.class)
            throw new InvalidReturnTypeException(String.format("Impossible d'envoyer une réponse sous le format \"JSON\" lorsque " +
                "le type de retour est \"void\": méthode \"%s\" du contrôleur \"%s\"", handlerMethod.getName(), controllerClass)
            );

        httpServletResponse.setContentType("application/json");
        ObjectMapper objectMapper = (ObjectMapper) webApplicationContainer.getManagedInstance(ThirdPartyConfiguration.JACKSON_OBJECT_MAPPER_ID);
        objectMapper.writeValue(httpServletResponse.getWriter(),
            handlerMethodResult instanceof ModelAndView modelAndView ? modelAndView.getData() : handlerMethodResult);
    }

    private void handleStringResult(
        HttpServletRequest  httpServletRequest,
        HttpServletResponse httpServletResponse,
        String originalString
    ) throws ServletException, IOException {
        originalString = originalString.strip();
        String string = "/" + originalString;
        if (!string.endsWith(".jsp")) string += ".jsp";

        if (httpServletRequest.getServletContext().getResource(string) != null) {
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

        httpServletResponse.sendRedirect(
            ((RedirectData) webApplicationContainer.getManagedInstance(RedirectData.MANAGED_INSTANCE_ID))
            .buildCompleteUrl(originalStringParts[1].stripLeading()));
    }
}
