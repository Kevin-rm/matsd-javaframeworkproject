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
import mg.matsd.javaframework.core.utils.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

class ResponseRenderer {
    private static final String ERROR_PAGE_TEMPLATE = """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta content="width=device-width, initial-scale=1.0" name="viewport">
            <title>Erreur</title>
            <style>
                @keyframes slide-in {
                    from {
                        transform: translateY(-20px);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0);
                        opacity: 1;
                    }
                }
    
                @keyframes fade-in {
                    from {
                        opacity: 0;
                    }
                    to {
                        opacity: 1;
                    }
                }
    
                @keyframes pulse {
                    0%% {
                        transform: scale(1);
                    }
                    50%% {
                        transform: scale(1.05);
                    }
                    100%% {
                        transform: scale(1);
                    }
                }
    
                @keyframes gradient-bg {
                    0%% {
                        background-position: 0 50%%;
                    }
                    50%% {
                        background-position: 100%% 50%%;
                    }
                    100%% {
                        background-position: 0 50%%;
                    }
                }
    
                :root {
                    --bg-primary: #0f172a;
                    --bg-secondary: #1e293b;
                    --bg-tertiary: #334155;
                    --text-primary: #f8fafc;
                    --text-secondary: #94a3b8;
                    --accent: #3b82f6;
                    --error: #ef4444;
                    --error-gradient: linear-gradient(135deg, #ef4444 0%%, #b91c1c 100%%);
                    --border: #475569;
                }
    
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
    
                body {
                    font-family: Nunito, sans-serif;
                    background: linear-gradient(135deg, var(--bg-primary) 0%%, #162544 100%%);
                    background-size: 400%% 400%%;
                    animation: gradient-bg 15s ease infinite;
                    color: var(--text-primary);
                    line-height: 1.6;
                    min-height: 100vh;
                    padding: 2rem;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
    
                .container {
                    width: 100%%;
                    max-width: 1000px;
                    animation: slide-in 0.6s ease-out;
                    background: var(--bg-secondary);
                    border-radius: 1rem;
                    overflow: hidden;
                    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
                    border: 1px solid var(--border);
                    backdrop-filter: blur(10px);
                }
    
                .header {
                    background: var(--error-gradient);
                    padding: 2rem;
                    position: relative;
                    overflow: hidden;
                }
    
                .header::before {
                    content: "";
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: linear-gradient(45deg, transparent 0%%, rgba(255, 255, 255, 0.1) 100%%);
                }
    
                .header h1 {
                    font-size: 1.8rem;
                    font-weight: 700;
                    margin: 0;
                    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                }
    
                .header p {
                    font-size: 1rem;
                    color: rgba(255, 255, 255, 0.9);
                    margin-top: 0.5rem;
                }
    
                .content {
                    padding: 2rem;
                    animation: fade-in 0.6s ease-out 0.3s both;
                }
    
                .content h2 {
                    font-size: 1.5rem;
                    font-weight: 600;
                    margin-bottom: 1rem;
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                }
    
                .content h2::before {
                    content: "";
                    width: 3px;
                    height: 1.25rem;
                    background: var(--accent);
                    border-radius: 4px;
                }
    
                .error-details {
                    background: var(--bg-tertiary);
                    border-radius: 0.75rem;
                    padding: 1.5rem;
                    margin-bottom: 1.5rem;
                    border: 1px solid var(--border);
                    transition: border-color 0.3s ease;
                }
    
                .error-details:hover {
                    border-color: var(--accent);
                }
    
                .error-detail-item {
                    display: flex;
                    align-items: center;
                    gap: 1rem;
                    padding: 1rem 0;
                    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
                    transition: background-color 0.2s ease;
                }
    
                .error-detail-item:hover {
                    background-color: rgba(255, 255, 255, 0.05);
                }
    
                .error-detail-item:last-child {
                    border-bottom: none;
                }
    
                .error-detail-label {
                    color: var(--text-secondary);
                    font-size: 0.875rem;
                    min-width: 140px;
                    font-weight: 500;
                }
    
                .error-detail-value {
                    color: var(--text-primary);
                    font-size: 1rem;
                    word-break: break-word;
                }
    
                .stacktrace-container {
                    position: relative;
                }
    
                .stacktrace {
                    background: var(--bg-tertiary);
                    border-radius: 0.75rem;
                    padding: 1.5rem;
                    border: 1px solid var(--border);
                    max-height: 400px;
                    overflow-y: auto;
                    transition: border-color 0.3s ease;
                    scrollbar-width: thin;
                    scrollbar-color: var(--accent) var(--bg-secondary);
                }
    
                .stacktrace:hover {
                    border-color: var(--accent);
                }
    
                .stacktrace::-webkit-scrollbar {
                    width: 12px;
                }
    
                .stacktrace::-webkit-scrollbar-track {
                    background: var(--bg-secondary);
                    border-radius: 6px;
                }
    
                .stacktrace::-webkit-scrollbar-thumb {
                    background: var(--accent);
                    border-radius: 6px;
                    border: 3px solid var(--bg-secondary);
                    transition: background-color 0.3s ease;
                }
    
                .stacktrace::-webkit-scrollbar-thumb:hover {
                    background: #60a5fa;
                }
    
                .stacktrace pre {
                    font-family: monospace;
                    font-size: 0.875rem;
                    color: var(--text-secondary);
                    white-space: pre-wrap;
                    word-break: break-all;
                    margin: 0;
                    line-height: 1.7;
                }
    
                #copy-button {
                    position: absolute;
                    top: 0.5rem;
                    right: 1rem;
                    background: var(--bg-secondary);
                    color: var(--text-primary);
                    border: 1px solid var(--border);
                    border-radius: 0.375rem;
                    padding: 0.5rem;
                    font-size: 0.75rem;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    display: flex;
                    align-items: center;
                    gap: 0.375rem;
                    z-index: 10;
                    backdrop-filter: blur(8px);
                }
    
                #copy-button:hover {
                    background: var(--bg-tertiary);
                    border-color: var(--accent);
                    transform: translateY(-1px);
                }
    
                #copy-button:active {
                    transform: translateY(0);
                }
    
                #copy-button svg {
                    width: 0.875rem;
                    height: 0.875rem;
                }
    
                #copy-button.copied {
                    background: #059669;
                    border-color: #059669;
                }
    
                #copy-button.copied svg {
                    stroke: white;
                }
    
                #copy-button .copy-icon,
                #copy-button .check-icon {
                    width: 0.875rem;
                    height: 0.875rem;
                }
    
                #copy-button .check-icon,
                #copy-button.copied .copy-icon {
                    display: none;
                }
    
                #copy-button.copied .check-icon {
                    display: block;
                }
    
                @media (max-width: 640px) {
                    body {
                        padding: 1rem;
                    }
    
                    .container {
                        margin: 0;
                    }
    
                    .header, .content {
                        padding: 1.5rem;
                    }
    
                    .error-details {
                        padding: 1rem;
                        margin-bottom: 1rem;
                    }
    
                    .error-detail-item {
                        flex-direction: column;
                        gap: 0.5rem;
                        padding: 0.75rem 0;
                    }
    
                    .error-detail-label {
                        min-width: auto;
                    }
    
                    #copy-button {
                        padding: 0.375rem;
                    }
                }
            </style>
        </head>
        <body>
        <div class="container">
            <div class="header">
                <h1>Une erreur s'est produite</h1>
                <p>Une exception a été rencontrée par le serveur. Veuillez examiner les informations ci-dessous pour le
                    débogage.</p>
            </div>
            <div class="content">
                <h2>Détails de l'erreur</h2>
                <div class="error-details">
                    <div class="error-detail-item">
                        <span class="error-detail-label">Type d'exception</span>
                        <span class="error-detail-value">%s</span>
                    </div>
                    <div class="error-detail-item">
                        <span class="error-detail-label">Message</span>
                        <span class="error-detail-value">%s</span>
                    </div>
                    <div class="error-detail-item">
                        <span class="error-detail-label">Code d'état HTTP</span>
                        <span class="error-detail-value">%d</span>
                    </div>
                </div>
                <h2>Pile d'appels</h2>
                <div class="stacktrace-container">
                    <button id="copy-button">
                        <svg class="copy-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
                                  stroke-linecap="round" stroke-linejoin="round"
                                  stroke-width="2"/>
                        </svg>
                        <svg class="check-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
                        </svg>
                        <span>Copier</span>
                    </button>
                    <div class="stacktrace">
                        <pre>%s</pre>
                    </div>
                </div>
            </div>
        </div>
    
        <script>
            document.addEventListener("DOMContentLoaded", () => {
                const copyButton     = document.getElementById("copy-button");
                const copyButtonSpan = copyButton.querySelector("span");
                const stacktrace     = document.querySelector(".stacktrace pre").innerText;
    
                copyButton.addEventListener("click", () => {
                    navigator.clipboard.writeText(stacktrace).then(() => {
                        copyButtonSpan.textContent = "Copié !";
                        copyButton.classList.add("copied");
    
                        setTimeout(() => {
                            copyButtonSpan.textContent = "Copier";
                            copyButton.classList.remove("copied");
                        }, 2000);
                    });
                });
            });
        </script>
        </body>
        </html>
    """;

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
        printWriter.write(String.format(ERROR_PAGE_TEMPLATE,
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
