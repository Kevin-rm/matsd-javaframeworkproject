package mg.itu.prom16.utils;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.JspFragment;
import mg.itu.prom16.validation.FieldError;
import mg.itu.prom16.validation.ModelBindingResult;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static mg.itu.prom16.utils.WebFacade.*;

public abstract class JspUtils {

    public static String routeTo(String name) throws JspException {
        Assert.notBlank(name, false, "Le nom de la route ne peut pas être vide ou \"null\"");

        return WebUtils.absolutePath(frontServlet.getRequestMappingInfoByName(name.strip()).getPath());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<FieldError> fieldErrors(final String propertyPath) {
        Assert.notBlank(propertyPath, false, "Le chemin vers la propriété ne peut pas être vide ou \"null\"");

        final String key = ModelBindingResult.FIELD_ERRORS_KEY_PREFIX + propertyPath;
        return (List<FieldError>) currentRequest().attribute(key);
    }

    public static boolean hasFlash(String key) {
        return flashBag().has(key);
    }

    @Nullable
    public static Object flash(String key) {
        return flashBag().get(key);
    }

    @Nullable
    public static Object flash(String key, @Nullable Object defaultValue) {
        return flashBag().get(key, defaultValue);
    }

    public static String invokeJspFragment(@Nullable JspFragment jspFragment) throws JspException, IOException {
        if (jspFragment == null) return "";

        StringWriter stringWriter = new StringWriter();
        jspFragment.invoke(stringWriter);
        return stringWriter.toString().trim();
    }
}
