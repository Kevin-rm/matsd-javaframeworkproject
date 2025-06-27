package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;

import java.util.Collection;
import java.util.Map;

public abstract class CollectionUtils {

    public static boolean isEmpty(@Nullable Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable Iterable<?> iterable) {
        return iterable == null ||
            (iterable instanceof Collection<?> collection ? collection.isEmpty() : !iterable.iterator().hasNext());
    }

    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
