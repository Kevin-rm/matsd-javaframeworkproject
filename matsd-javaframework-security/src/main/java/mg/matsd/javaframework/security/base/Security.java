package mg.matsd.javaframework.security.base;

import jakarta.servlet.Filter;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Security {
    private static final List<Filter> FILTERS = new ArrayList<>();

    public Security addFilter(@Nullable Filter... filters) {
        if (filters == null) return this;
        Assert.noNullElements(filters, "Chaque filtre du tableau de filtres ne peut être \"null\"");

        Collections.addAll(FILTERS, filters);
        return this;
    }
}
