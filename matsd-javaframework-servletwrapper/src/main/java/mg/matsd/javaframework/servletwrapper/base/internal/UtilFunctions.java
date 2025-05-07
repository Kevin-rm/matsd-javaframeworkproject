package mg.matsd.javaframework.servletwrapper.base.internal;

import mg.matsd.javaframework.core.utils.Assert;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class UtilFunctions {

    public static Map<String, Object> collectAttributes(
        Enumeration<String> attributeNames, Function<String, Object> valueGetter
    ) {
        Assert.notNull(attributeNames);
        Assert.notNull(valueGetter);

        Map<String, Object> attributes = new HashMap<>();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            attributes.put(key, valueGetter.apply(key));
        }

        return Collections.unmodifiableMap(attributes);
    }
}
