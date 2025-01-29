package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;

public final class ArrayUtils {
    private ArrayUtils() { }

    public static <T> boolean isEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }
}
