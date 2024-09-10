package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

class SessionFactoryOptionsRegistry {
    private final List<SessionFactoryOptions> sessionFactoryOptionsList;

    SessionFactoryOptionsRegistry() {
        sessionFactoryOptionsList = new ArrayList<>();
    }

    List<SessionFactoryOptions> getSessionFactoryOptionsList() {
        return sessionFactoryOptionsList;
    }

    SessionFactoryOptions getFirst() throws IllegalStateException {
        Assert.state(!sessionFactoryOptionsList.isEmpty());

        return sessionFactoryOptionsList.get(0);
    }

    @Nullable
    SessionFactoryOptions getSessionFactoryOptionsByName(@Nullable String name) {
        return sessionFactoryOptionsList
            .stream()
            .filter(sessionFactoryOptions -> {
                String sessionFactoryOptionsName = sessionFactoryOptions.getName();

                return sessionFactoryOptionsName != null && sessionFactoryOptionsName.equals(name);
            })
            .findFirst()
            .orElse(null);
    }

    SessionFactoryOptions getSessionFactoryOptions(@Nullable String name) throws IllegalStateException, ConfigurationException {
        if (name == null || StringUtils.isBlank(name))
            return getFirst();

        SessionFactoryOptions sessionFactoryOptions = getSessionFactoryOptionsByName(name);
        if (sessionFactoryOptions == null)
            throw new ConfigurationException(String.format("Aucune \"session factory\" trouvée avec le nom spécifié : \"%s\"", name));

        return sessionFactoryOptions;
    }

    SessionFactoryOptions getSessionFactoryOptionsOrRegisterIfAbsent(@Nullable String name) {
        try {
            return getSessionFactoryOptions(name);
        } catch (Exception ignored) {
            return registerSessionFactoryOptions(name);
        }
    }

    SessionFactoryOptions registerSessionFactoryOptions(@Nullable String name) {
        if (containsSessionFactoryOptions(name))
            throw new ConfigurationException(String.format("Duplication détectée pour le nom d'une \"session factory\" : \"%s\"", name));

        SessionFactoryOptions sessionFactoryOptions = new SessionFactoryOptions(count() + 1, name);
        sessionFactoryOptionsList.add(sessionFactoryOptions);

        return sessionFactoryOptions;
    }

    boolean containsSessionFactoryOptions(@Nullable String name) {
        return getSessionFactoryOptionsByName(name) != null;
    }

    int count() {
        return sessionFactoryOptionsList.size();
    }
}
