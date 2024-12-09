package mg.itu.prom16.utils;

import jakarta.servlet.ServletException;
import mg.itu.prom16.base.FrontServlet;
import mg.itu.prom16.http.FlashBag;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.http.SessionImpl;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.itu.prom16.validation.FieldError;
import mg.itu.prom16.validation.ModelBindingResult;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.List;

public final class JspUtils {
    private static FrontServlet frontServlet;

    private JspUtils() { }

    public static void setFrontServlet(final FrontServlet frontServlet) {
        Assert.notNull(frontServlet);

        JspUtils.frontServlet = frontServlet;
    }

    public static String display(@Nullable Object object) {
        return object == null ? "" : object.toString();
    }

    public static String routeTo(String name) throws ServletException {
        Assert.notBlank(name, false, "Le nom de la route ne peut pas être vide ou \"null\"");

        return WebUtils.absolutePath(frontServlet.getRequestMappingInfoByName(name.strip()).getPath());
    }

    @SuppressWarnings("unchecked")
    public static boolean hasFieldErrors(final String propertyPath) {
        Assert.notBlank(propertyPath, false, "Le chemin vers la propriété ne peut pas être vide ou \"null\"");

        final String key = ModelBindingResult.FIELD_ERRORS_KEY_PREFIX + propertyPath;
        List<FieldError> fieldErrors = (List<FieldError>) WebFacade.getCurrentRequest().getAttribute(key);
        return fieldErrors != null || getFlashBag().has(key);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<FieldError> getFieldErrors(final String propertyPath) {
        Assert.notBlank(propertyPath, false, "Le chemin vers la propriété ne peut pas être vide ou \"null\"");

        final String key = ModelBindingResult.FIELD_ERRORS_KEY_PREFIX + propertyPath;
        List<FieldError> fieldErrors = (List<FieldError>) WebFacade.getCurrentRequest().getAttribute(key);
        return fieldErrors == null ? (List<FieldError>) flash(key) : fieldErrors;
    }

    @Nullable
    public static Object flash(String key) {
        return getFlashBag().get(key);
    }

    @Nullable
    public static Object flash(String key, @Nullable Object defaultValue) {
        return getFlashBag().get(key, defaultValue);
    }

    private static FlashBag getFlashBag() {
        return ((Session) WebFacade.getCurrentSession()
            .getAttribute(WebApplicationContainer.WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + SessionImpl.MANAGED_INSTANCE_ID)
        ).getFlashBag();
    }
}
