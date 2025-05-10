import type { ErrorData } from '../types';

export const mockErrorData: ErrorData = {
  type: "java.lang.NullPointerException",
  message: "Cannot invoke \"mg.itu.prom16.base.Model.setAttributes(mg.matsd.javaframework.servletwrapper.http.Request)\" because \"model\" is null",
  statusCode: 500,
  trace: [
    {
      file: "mg/itu/prom16/base/ResponseRenderer.java",
      line: 68,
      method: "doRender",
      fileIndex: 0
    },
    {
      file: "mg/itu/prom16/base/FrontServlet.java",
      line: 156,
      method: "processRequest",
      fileIndex: 1
    },
    {
      file: "mg/itu/prom16/base/FrontServlet.java",
      line: 189,
      method: "service",
      fileIndex: 1
    }
  ],
  files: [
    {
      path: "mg/itu/prom16/base/ResponseRenderer.java",
      content: `package mg.itu.prom16.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import mg.itu.prom16.base.internal.handler.AbstractHandler;
import mg.itu.prom16.exceptions.InvalidReturnTypeException;

// More imports removed for brevity

class ResponseRenderer {
    private static final String ERROR_PAGE_TEMPLATE_FILE = "error-page-template.txt";
    private static final String ERROR_PAGE_TEMPLATE_CONTENT;

    static {
        try (
            ClassPathResource classPathResource = new ClassPathResource(ERROR_PAGE_TEMPLATE_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(classPathResource.getInputStream(), StandardCharsets.UTF_8))
        ) {
            ERROR_PAGE_TEMPLATE_CONTENT = bufferedReader.lines().collect(Collectors.joining("\\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final WebApplicationContainer webApplicationContainer;

    ResponseRenderer(final WebApplicationContainer webApplicationContainer) {
        this.webApplicationContainer = webApplicationContainer;
    }

    void doRender(
        Request request,
        Response response,
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
}`,
      highlight: {
        line: 47,
        startColumn: 9,
        endColumn: 36
      }
    },
    {
      path: "mg/itu/prom16/base/FrontServlet.java",
      content: `package mg.itu.prom16.base;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import mg.itu.prom16.annotations.JsonResponse;

// More imports removed for brevity

public class FrontServlet extends HttpServlet {
    private static final Logger LOGGER = LogManager.getLogger(FrontServlet.class);

    private static Throwable throwableOnInit;
    private WebApplicationContainer webApplicationContainer;
    private ResponseRenderer responseRenderer;
    
    // Code removed for brevity

    protected final void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws ServletException, IOException {
        final Request request = new Request(httpServletRequest);
        final Response response = new Response(httpServletResponse, request).setCharset("UTF-8");

        if (throwableOnInit != null) {
            ResponseRenderer.doRenderError(throwableOnInit, response);
            LOGGER.fatal("Une erreur s'est produite durant l'initialisation du \\"FrontServlet\\"", throwableOnInit);
            return;
        }

        RequestContextHolder.setRequestContext(new RequestContext(request, response));
        final Session session = request.getSession(true);

        MappingHandler mappingHandler = null;
        try {
            final String servletPath = request.getServletPath();
            final String requestMethod = request.getMethod();

            Map.Entry<RequestMappingInfo, MappingHandler> mappingHandlerEntry = resolveMappingHandler(servletPath, RequestMethod.valueOf(requestMethod));
            if (mappingHandlerEntry == null)
                throw new NotFoundHttpException(String.format("Aucun mapping trouvé pour le path : \\"%s\\" et method : \\"%s\\"",
                    servletPath, requestMethod));
            mappingHandler = mappingHandlerEntry.getValue();

            // Authorization code removed for brevity

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

    // More methods removed for brevity
}`,
      highlight: null
    }
  ]
};
