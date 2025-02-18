package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;

public abstract class ArrayUtils {

    public static <T> boolean isEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }
}
