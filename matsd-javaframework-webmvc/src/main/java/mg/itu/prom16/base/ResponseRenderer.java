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
import mg.itu.prom16.support.ThirdPartyConfiguration;
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
        printWriter.println("<!DOCTYPE html>");
        printWriter.println("<html lang=\"fr\">");
        printWriter.println("<head>");
        printWriter.println("   <meta charset=\"UTF-8\">");
        printWriter.println("   <meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\">");
        printWriter.println("   <title>Erreur</title>");
        printWriter.println("   <style>");
        printWriter.println("       @keyframes slideIn {");
        printWriter.println("           from {");
        printWriter.println("               transform: translateY(-20px);");
        printWriter.println("               opacity: 0;");
        printWriter.println("           }");
        printWriter.println("           to {");
        printWriter.println("               transform: translateY(0);");
        printWriter.println("               opacity: 1;");
        printWriter.println("           }");
        printWriter.println("       }");
        printWriter.println("       @keyframes fadeIn {");
        printWriter.println("           from {");
        printWriter.println("               opacity: 0;");
        printWriter.println("           }");
        printWriter.println("           to {");
        printWriter.println("               opacity: 1;");
        printWriter.println("           }");
        printWriter.println("       }");
        printWriter.println("       @keyframes pulse {");
        printWriter.println("           0% {");
        printWriter.println("               transform: scale(1);");
        printWriter.println("           }");
        printWriter.println("           50% {");
        printWriter.println("               transform: scale(1.05);");
        printWriter.println("           }");
        printWriter.println("           100% {");
        printWriter.println("               transform: scale(1);");
        printWriter.println("           }");
        printWriter.println("       }");
        printWriter.println("       @keyframes gradientBG {");
        printWriter.println("           0% {");
        printWriter.println("               background-position: 0 50%;");
        printWriter.println("           }");
        printWriter.println("           50% {");
        printWriter.println("               background-position: 100% 50%;");
        printWriter.println("           }");
        printWriter.println("           100% {");
        printWriter.println("               background-position: 0 50%;");
        printWriter.println("           }");
        printWriter.println("       }");
        printWriter.println("       :root {");
        printWriter.println("           --bg-primary: #0f172a;");
        printWriter.println("           --bg-secondary: #1e293b;");
        printWriter.println("           --bg-tertiary: #334155;");
        printWriter.println("           --text-primary: #f8fafc;");
        printWriter.println("           --text-secondary: #94a3b8;");
        printWriter.println("           --accent: #3b82f6;");
        printWriter.println("           --error: #ef4444;");
        printWriter.println("           --error-gradient: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%);");
        printWriter.println("           --border: #475569;");
        printWriter.println("       }");
        printWriter.println("       * {");
        printWriter.println("           margin: 0;");
        printWriter.println("           padding: 0;");
        printWriter.println("           box-sizing: border-box;");
        printWriter.println("       }");
        printWriter.println("       body {");
        printWriter.println("           font-family: 'Nunito', sans-serif;");
        printWriter.println("           background: linear-gradient(135deg, var(--bg-primary) 0%, #162544 100%);");
        printWriter.println("           background-size: 400% 400%;");
        printWriter.println("           animation: gradientBG 15s ease infinite;");
        printWriter.println("           color: var(--text-primary);");
        printWriter.println("           line-height: 1.6;");
        printWriter.println("           min-height: 100vh;");
        printWriter.println("           padding: 2rem;");
        printWriter.println("           display: flex;");
        printWriter.println("           align-items: center;");
        printWriter.println("           justify-content: center;");
        printWriter.println("       }");
        printWriter.println("       .container {");
        printWriter.println("           width: 100%;");
        printWriter.println("           max-width: 1000px;");
        printWriter.println("           animation: slideIn 0.6s ease-out;");
        printWriter.println("           background: var(--bg-secondary);");
        printWriter.println("           border-radius: 1rem;");
        printWriter.println("           overflow: hidden;");
        printWriter.println("           box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);");
        printWriter.println("           border: 1px solid var(--border);");
        printWriter.println("           backdrop-filter: blur(10px);");
        printWriter.println("       }");
        printWriter.println("       .header {");
        printWriter.println("           background: var(--error-gradient);");
        printWriter.println("           padding: 2rem;");
        printWriter.println("           position: relative;");
        printWriter.println("           overflow: hidden;");
        printWriter.println("       }");
        printWriter.println("       .header::before {");
        printWriter.println("           content: '';");
        printWriter.println("           position: absolute;");
        printWriter.println("           top: 0;");
        printWriter.println("           left: 0;");
        printWriter.println("           right: 0;");
        printWriter.println("           bottom: 0;");
        printWriter.println("           background: linear-gradient(45deg, transparent 0%, rgba(255, 255, 255, 0.1) 100%);");
        printWriter.println("       }");
        printWriter.println("       .header h1 {");
        printWriter.println("           font-size: 1.8rem;");
        printWriter.println("           font-weight: 700;");
        printWriter.println("           margin: 0;");
        printWriter.println("           text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);");
        printWriter.println("       }");
        printWriter.println("       .header p {");
        printWriter.println("           font-size: 1rem;");
        printWriter.println("           color: rgba(255, 255, 255, 0.9);");
        printWriter.println("           margin-top: 0.5rem;");
        printWriter.println("       }");
        printWriter.println("       .content {");
        printWriter.println("           padding: 2rem;");
        printWriter.println("           animation: fadeIn 0.6s ease-out 0.3s both;");
        printWriter.println("       }");
        printWriter.println("       .content h2 {");
        printWriter.println("           font-size: 1.5rem;");
        printWriter.println("           font-weight: 600;");
        printWriter.println("           margin-bottom: 1rem;");
        printWriter.println("           display: flex;");
        printWriter.println("           align-items: center;");
        printWriter.println("           gap: 0.5rem;");
        printWriter.println("       }");
        printWriter.println("       .content h2::before {");
        printWriter.println("           content: '';");
        printWriter.println("           width: 3px;");
        printWriter.println("           height: 1.25rem;");
        printWriter.println("           background: var(--accent);");
        printWriter.println("           border-radius: 4px;");
        printWriter.println("       }");
        printWriter.println("       .error-details {");
        printWriter.println("           background: var(--bg-tertiary);");
        printWriter.println("           border-radius: 0.75rem;");
        printWriter.println("           padding: 1.5rem;");
        printWriter.println("           margin-bottom: 1.5rem;");
        printWriter.println("           border: 1px solid var(--border);");
        printWriter.println("           transition: border-color 0.3s ease;");
        printWriter.println("       }");
        printWriter.println("       .error-details:hover {");
        printWriter.println("           border-color: var(--accent);");
        printWriter.println("       }");
        printWriter.println("       .detail-item {");
        printWriter.println("           display: flex;");
        printWriter.println("           align-items: center;");
        printWriter.println("           gap: 1rem;");
        printWriter.println("           padding: 1rem 0;");
        printWriter.println("           border-bottom: 1px solid rgba(255, 255, 255, 0.1);");
        printWriter.println("           transition: background-color 0.2s ease;");
        printWriter.println("       }");
        printWriter.println("       .detail-item:hover {");
        printWriter.println("           background-color: rgba(255, 255, 255, 0.05);");
        printWriter.println("       }");
        printWriter.println("       .detail-item:last-child {");
        printWriter.println("           border-bottom: none;");
        printWriter.println("       }");
        printWriter.println("       .detail-label {");
        printWriter.println("           color: var(--text-secondary);");
        printWriter.println("           font-size: 0.875rem;");
        printWriter.println("           min-width: 140px;");
        printWriter.println("           font-weight: 500;");
        printWriter.println("       }");
        printWriter.println("       .detail-value {");
        printWriter.println("           color: var(--text-primary);");
        printWriter.println("           font-size: 1rem;");
        printWriter.println("           word-break: break-word;");
        printWriter.println("       }");
        printWriter.println("       .stacktrace {");
        printWriter.println("           background: var(--bg-tertiary);");
        printWriter.println("           border-radius: 0.75rem;");
        printWriter.println("           padding: 1.5rem;");
        printWriter.println("           border: 1px solid var(--border);");
        printWriter.println("           max-height: 400px;");
        printWriter.println("           overflow-y: auto;");
        printWriter.println("           transition: border-color 0.3s ease;");
        printWriter.println("       }");
        printWriter.println("       .stacktrace:hover {");
        printWriter.println("           border-color: var(--accent);");
        printWriter.println("       }");
        printWriter.println("       .stacktrace::-webkit-scrollbar {");
        printWriter.println("           width: 8px;");
        printWriter.println("           height: 8px;");
        printWriter.println("       }");
        printWriter.println("       .stacktrace::-webkit-scrollbar-track {");
        printWriter.println("           background: var(--bg-secondary);");
        printWriter.println("       }");
        printWriter.println("       .stacktrace::-webkit-scrollbar-thumb {");
        printWriter.println("           background: var(--border);");
        printWriter.println("           transition: background-color 0.3s ease;");
        printWriter.println("       }");
        printWriter.println("       .stacktrace::-webkit-scrollbar-thumb:hover {");
        printWriter.println("           background: var(--accent);");
        printWriter.println("       }");
        printWriter.println("       pre {");
        printWriter.println("           font-family: monospace;");
        printWriter.println("           font-size: 0.875rem;");
        printWriter.println("           color: var(--text-secondary);");
        printWriter.println("           white-space: pre-wrap;");
        printWriter.println("           word-break: break-all;");
        printWriter.println("           margin: 0;");
        printWriter.println("           line-height: 1.7;");
        printWriter.println("       }");
        printWriter.println("       @media (max-width: 640px) {");
        printWriter.println("           body {");
        printWriter.println("               padding: 1rem;");
        printWriter.println("           }");
        printWriter.println("           .container {");
        printWriter.println("               margin: 0;");
        printWriter.println("           }");
        printWriter.println("           .header {");
        printWriter.println("               padding: 1.5rem;");
        printWriter.println("           }");
        printWriter.println("           .content {");
        printWriter.println("               padding: 1.5rem;");
        printWriter.println("           }");
        printWriter.println("           .error-details {");
        printWriter.println("               padding: 1rem;");
        printWriter.println("               margin-bottom: 1rem;");
        printWriter.println("           }");
        printWriter.println("           .detail-item {");
        printWriter.println("               flex-direction: column;");
        printWriter.println("               gap: 0.5rem;");
        printWriter.println("               padding: 0.75rem 0;");
        printWriter.println("           }");
        printWriter.println("           .detail-label {");
        printWriter.println("               min-width: auto;");
        printWriter.println("           }");
        printWriter.println("       }");
        printWriter.println("   </style>");
        printWriter.println("</head>");
        printWriter.println("<body>");
        printWriter.println("<div class=\"container\">");
        printWriter.println("   <div class=\"header\">");
        printWriter.println("       <h1>Une erreur s'est produite.</h1>");
        printWriter.println("       <p>Une exception a été rencontrée par le serveur. Veuillez examiner les informations ci-dessous pour le débogage.</p>");
        printWriter.println("   </div>");
        printWriter.println("   <div class=\"content\">");
        printWriter.println("       <h2>Détails de l'erreur</h2>");
        printWriter.println("       <div class=\"error-details\">");
        printWriter.println("           <div class=\"detail-item\">");
        printWriter.println("               <span class=\"detail-label\">Type d'exception</span>");
        printWriter.println(String.format("               <span class=\"detail-value\">%s</span>", throwable.getClass().getName()));
        printWriter.println("           </div>");
        printWriter.println("           <div class=\"detail-item\">");
        printWriter.println("               <span class=\"detail-label\">Message</span>");
        printWriter.println(String.format("               <span class=\"detail-value\">%s</span>", throwable.getMessage()));
        printWriter.println("           </div>");
        printWriter.println("           <div class=\"detail-item\">");
        printWriter.println("               <span class=\"detail-label\">Code d'état HTTP</span>");
        printWriter.println(String.format("               <span class=\"detail-value\">%d</span>", httpServletResponse.getStatus()));
        printWriter.println("           </div>");
        printWriter.println("       </div>");
        printWriter.println("       <h2>Pile d'appels</h2>");
        printWriter.println("       <div class=\"stacktrace\">");
        printWriter.println("           <pre>");
        throwable.printStackTrace(printWriter);
        printWriter.println("           </pre>");
        printWriter.println("       </div>");
        printWriter.println("   </div>");
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

        originalStringParts[1] = originalStringParts[1].stripLeading();
        if (UtilFunctions.isAbsoluteUrl(originalStringParts[1])) {
            httpServletResponse.sendRedirect(originalStringParts[1]);
            return;
        }

        httpServletResponse.sendRedirect(WebUtils.absolutePath(originalStringParts[1]));
    }
}
