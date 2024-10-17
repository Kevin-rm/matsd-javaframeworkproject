package mg.itu.prom16.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.base.internal.handler.AbstractHandler;
import mg.itu.prom16.exceptions.InvalidReturnTypeException;
import mg.itu.prom16.exceptions.NotFoundHttpException;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.utils.WebUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

class ResponseRenderer {
    private final WebApplicationContainer webApplicationContainer;

    ResponseRenderer(final WebApplicationContainer webApplicationContainer) {
        this.webApplicationContainer = webApplicationContainer;
    }

    static void doRenderError(Throwable throwable, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("text/html");
        httpServletResponse.setStatus(throwable instanceof NotFoundHttpException ?
            NotFoundHttpException.statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        PrintWriter printWriter = httpServletResponse.getWriter();
        printWriter.println("<html>");
        printWriter.println("<head>");
        printWriter.println("<title>Erreur</title>");
        printWriter.println("<style>");
        printWriter.println("body { font-family: 'Arial', sans-serif; background-color: #1a1a1a; color: #cccccc; margin: 0; display: flex; justify-content: center; align-items: center; height: 100vh; }");
        printWriter.println(".container { max-width: 850px; width: 100%; margin: 20px auto; padding: 30px; background-color: #2a2a2a; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.7); border-radius: 10px; }");
        printWriter.println("h1 { color: #ff9999; font-size: 28px; margin-top: 0; margin-bottom: 10px; }");
        printWriter.println("h2 { color: #dddddd; font-size: 22px; margin-top: 20px; margin-bottom: 10px; }");
        printWriter.println("p { line-height: 1.6; font-size: 14px; margin: 5px auto; }");
        printWriter.println(".stacktrace { background-color: #333333; padding: 15px; border: 1px solid #555555; overflow: auto; max-height: 380px; border-radius: 6px; box-shadow: inset 0 1px 2px rgba(255, 255, 255, 0.1); margin-top: 20px; text-align: left; }");
        printWriter.println("pre { font-family: 'Roboto Mono', monospace; font-size: 12px; white-space: pre-wrap; word-wrap: break-word; margin: 0; color: #ff9999; }");
        printWriter.println(".fade-in { animation: fadeIn 0.2s ease-in-out; }");
        printWriter.println("@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }");
        printWriter.println("</style>");
        printWriter.println("</head>");
        printWriter.println("<body>");
        printWriter.println("<div class=\"container fade-in\">");
        printWriter.println("<h1>Une erreur s'est produite</h1>");
        printWriter.println("<p>Une exception a été rencontrée par le serveur. Veuillez examiner les informations ci-dessous pour le débogage.</p>");
        printWriter.println("<h2>Détails de l'erreur</h2>");
        printWriter.println(String.format("<p><strong>Type d'exception:</strong> %s</p>", throwable.getClass().getName()));
        printWriter.println(String.format("<p><strong>Message:</strong> %s</p>", throwable.getMessage()));
        printWriter.println(String.format("<p><strong>Code d'état HTTP:</strong> %d</p>", httpServletResponse.getStatus()));
        printWriter.println("<div class=\"stacktrace\">");
        printWriter.println("<pre>");
        throwable.printStackTrace(printWriter);
        printWriter.println("</pre>");
        printWriter.println("</div>");
        printWriter.println("</div>");
        printWriter.println("</body>");
        printWriter.println("</html>");

        printWriter.flush();
    }

    void doRender(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Session session,
        AbstractHandler handler,
        Object additionalParameter
    ) throws ServletException, IOException {
        Object handlerMethodResult = handler.invokeMethod(
            webApplicationContainer, httpServletRequest, httpServletResponse, session, additionalParameter);
        Method handlerMethod = handler.getMethod();

        Model model = (Model) webApplicationContainer.getManagedInstance(Model.MANAGED_INSTANCE_ID);
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
        ObjectMapper objectMapper = (ObjectMapper) webApplicationContainer.getManagedInstance(WebApplicationContainer.JACKSON_OBJECT_MAPPER_ID);
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

        originalStringParts[1] = originalStringParts[1].stripLeading();
        if (UtilFunctions.isAbsoluteUrl(originalStringParts[1])) {
            httpServletResponse.sendRedirect(originalStringParts[1]);
            return;
        }

        httpServletResponse.sendRedirect(WebUtils.absolutePath(originalStringParts[1]));
    }
}
