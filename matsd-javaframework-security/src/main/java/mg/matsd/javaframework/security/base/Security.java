package mg.matsd.javaframework.security.base;

import jakarta.servlet.Filter;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Security {
    private final List<Filter> filters;

    public Security() {
        filters = new ArrayList<>();
    }

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
            filters.add(index, filter);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format("Index spécifié invalide: %d", index));
        }

        return this;
    }

    public synchronized Security addFilterBefore(Filter filter, Class<? extends Filter> filterClass) {
        return addFilter(filterClass, filter, true);
    }

    public synchronized Security addFilterAfter(Filter filter, Class<? extends Filter> filterClass) {
        return addFilter(filterClass, filter, false);
    }

    public synchronized Security appendFilter(@Nullable Filter... filters) {
        if (filters == null) return this;
        Assert.noNullElements(filters, "Chaque filtre du tableau de filtres ne peut être \"null\"");

        Collections.addAll(this.filters, filters);
        return this;
    }

    private synchronized Security addFilter(Class<? extends Filter> filterClass, Filter filter, boolean before) {
        Assert.notNull(filterClass, "La classe du filtre ne peut pas être \"null\"");

        for (Filter f : filters) {
            if (!filterClass.isInstance(f)) continue;

            filters.add(filters.indexOf(f) + (before ? 0 : 1), filter);
            return this;
        }

        throw new IllegalArgumentException(String.format("Le filtre de classe \"%s\" n'existe pas dans le tableau de filtres",
            filterClass.getName()));
    }
}
