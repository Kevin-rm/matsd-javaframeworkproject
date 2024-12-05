package mg.itu.prom16.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.FrontServlet;
import mg.itu.prom16.validation.ModelBindingResult;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.base.internal.request.ServletRequestAttributes;
import mg.itu.prom16.http.FlashBag;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.http.SessionImpl;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.base.ConstraintViolation;

import java.util.List;

public final class WebFacade {
    private static FrontServlet frontServlet;

    private WebFacade() { }

    public static void setFrontServlet(final FrontServlet frontServlet) {
        Assert.notNull(frontServlet);

        WebFacade.frontServlet = frontServlet;
    }

    public static HttpServletRequest getCurrentRequest() {
        return getServletRequestAttributes().getRequest();
    }

    public static boolean isGetRequest() {
        return "GET".equals(getCurrentRequest().getMethod());
    }

    public static boolean isPostRequest() {
        return "POST".equals(getCurrentRequest().getMethod());
    }

    @Nullable
    public static Object flash(String key) {
        return getFlashBag().get(key);
    }

    @Nullable
    public static Object flash(String key, @Nullable Object defaultValue) {
        return getFlashBag().get(key, defaultValue);
    }

    public static String routeTo(String name) throws ServletException {
        Assert.notBlank(name, false, "Le nom de la route ne peut pas être vide ou \"null\"");

        return WebUtils.absolutePath(frontServlet.getRequestMappingInfoByName(name.strip()).getPath());
    }

    public static boolean hasConstraintViolations(final String modelName, final String propertyPath) {
        ModelBindingResult modelBindingResult = getModelBindingResult();
        return modelBindingResult != null && modelBindingResult.hasConstraintViolations(modelName, propertyPath);
    }

    @Nullable
    public static List<ConstraintViolation<?>> getConstraintViolations(final String modelName, final String propertyPath) {
        ModelBindingResult modelBindingResult = getModelBindingResult();
        return modelBindingResult != null ? modelBindingResult.getConstraintViolations(modelName, propertyPath) : null;
    }

    @Nullable
    private static ModelBindingResult getModelBindingResult() {
        ModelBindingResult modelBindingResult = (ModelBindingResult) getCurrentRequest().getAttribute(ModelBindingResult.STORAGE_KEY);
        return modelBindingResult == null ? (ModelBindingResult) getFlashBag().get(ModelBindingResult.STORAGE_KEY) : modelBindingResult;
    }

    private static FlashBag getFlashBag() {
        return ((Session) getServletRequestAttributes().getSession()
            .getAttribute(WebApplicationContainer.WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + SessionImpl.MANAGED_INSTANCE_ID)
        ).getFlashBag();
    }

    private static ServletRequestAttributes getServletRequestAttributes() {
        return RequestContextHolder.getServletRequestAttributes();
    }
}
