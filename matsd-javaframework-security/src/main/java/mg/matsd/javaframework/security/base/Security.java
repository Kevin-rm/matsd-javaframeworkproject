package mg.matsd.javaframework.security.base;

import com.sun.jdi.InternalException;
import jakarta.servlet.Filter;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Security {
    private static final List<Filter> FILTERS = new ArrayList<>();

    @Nullable
    private AuthenticationManager authenticationManager;

    @Nullable
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public Security setAuthenticationManager(@Nullable AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }

    public synchronized Security addFilter(int index, Filter filter) {
        Assert.notNull(filter, "Le filtre ne peut pas être \"null\"");

        try {
            FILTERS.add(index, filter);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format("L'index spécifié (=%d) est invalide", index));
        }

        return this;
    }

    public synchronized Security addFilterBefore(Class<? extends Filter> filterClass) {
        return addFilter(filterClass, "BEFORE");
    }

    public synchronized Security addFilterAfter(Class<? extends Filter> filterClass) {
        return addFilter(filterClass, "AFTER");
    }

    public synchronized Security appendFilter(@Nullable Filter... filters) {
        if (filters == null) return this;
        Assert.noNullElements(filters, "Chaque filtre du tableau de filtres ne peut être \"null\"");

        Collections.addAll(FILTERS, filters);
        return this;
    }

    private synchronized Security addFilter(Class<? extends Filter> filterClass, String position) {
        Assert.notNull(filterClass, "La classe du filtre ne peut pas être \"null\"");

        for (Filter filter : FILTERS) {
            if (!filterClass.isInstance(filter)) continue;

            final int index = FILTERS.indexOf(filter);
            switch (position) {
                case "BEFORE" -> FILTERS.add(index, filter);
                case "AFTER"  -> FILTERS.add(index + 1, filter);
                default       -> throw new InternalException();
            }

            return this;
        }

        throw new IllegalArgumentException(String.format("Le filtre de classe \"%s\" n'existe pas dans le tableau de filtres",
            filterClass.getName()));
    }
}
