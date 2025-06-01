package mg.matsd.javaframework.servletwrapper.base.internal;

import mg.matsd.javaframework.core.utils.Assert;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class UtilFunctions {

    public static Map<String, ?> collectKeyValues(
        final Enumeration<String> keys,
        final Function<String, ?> valueGetter
    ) {
        Assert.notNull(keys);
        Assert.notNull(valueGetter);

        final Map<String, Object> keyValues = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            keyValues.put(key, valueGetter.apply(key));
        }

        return Collections.unmodifiableMap(keyValues);
    }
}
