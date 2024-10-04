package mg.itu.prom16.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.base.internal.handler.AbstractHandler;
import mg.itu.prom16.exceptions.InvalidReturnTypeException;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.utils.WebUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

public class ResponseRenderer {
    private WebApplicationContainer webApplicationContainer;

    public void setWebApplicationContainer(WebApplicationContainer webApplicationContainer) {
        Assert.notNull(webApplicationContainer, "L'argument webApplicationContainer ne peut pas être \"null\"");

        this.webApplicationContainer = webApplicationContainer;
    }

    void doRender(
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
