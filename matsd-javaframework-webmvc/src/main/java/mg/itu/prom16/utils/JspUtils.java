package mg.itu.prom16.utils;

import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.http.FlashBag;
import mg.itu.prom16.http.Session;
import mg.itu.prom16.http.SessionImpl;
import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;

public final class JspUtils {

    private JspUtils() { }

    @Nullable
    public static String[] flashMessages(String key) {
        return getFlashBag().get(key);
    }

    @Nullable
    public static String[] flashMessages(String key, @Nullable String[] defaultValue) {
        return getFlashBag().get(key, defaultValue);
    }

    private static FlashBag getFlashBag() {
        return ((Session) RequestContextHolder
            .getServletRequestAttributes().getSession()
            .getAttribute(WebApplicationContainer.WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + SessionImpl.MANAGED_INSTANCE_ID)
        ).getFlashBag();
    }
}
