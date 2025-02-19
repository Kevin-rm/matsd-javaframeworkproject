package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.Collection;

public abstract class ArrayUtils {

    public static <T> boolean isEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean isEmpty(@Nullable Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }
}
