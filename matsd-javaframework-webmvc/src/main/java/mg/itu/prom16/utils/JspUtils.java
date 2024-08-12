package mg.itu.prom16.utils;

import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.http.FlashBag;
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
        return RequestContextHolder.getServletRequestAttributes()
            .getSession()
            .getFlashBag();
    }
}
