package mg.itu.prom16.utils;

import jakarta.servlet.jsp.JspException;
import mg.itu.prom16.base.FrontServlet;
import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.http.FlashBag;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.http.SessionImpl;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

public final class JspUtils {
    private static FrontServlet frontServlet;

    public static void setFrontServlet(final FrontServlet frontServlet) {
        Assert.notNull(frontServlet);

        JspUtils.frontServlet = frontServlet;
    }

    private JspUtils() { }

    @Nullable
    public static String[] flashMessages(String key) {
        return getFlashBag().get(key);
    }

    @Nullable
    public static String[] flashMessages(String key, @Nullable String[] defaultValue) {
        return getFlashBag().get(key, defaultValue);
    }

    public static String routeTo(String name) throws JspException {
        Assert.notBlank(name, false, "Le nom de la route ne peut pas être vide ou \"null\"");

        RequestMappingInfo requestMappingInfo = frontServlet.getRequestMappingInfoByName(name.strip());
        if (requestMappingInfo == null)
            throw new JspException(String.format("Aucun \"RequestMapping\" trouvé avec le nom : \"%s\"", name));

        return WebUtils.absolutePath(requestMappingInfo.getPath());
    }

    private static FlashBag getFlashBag() {
        return ((Session) RequestContextHolder
            .getServletRequestAttributes().getSession()
            .getAttribute(WebApplicationContainer.WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + SessionImpl.MANAGED_INSTANCE_ID)
        ).getFlashBag();
    }
}
