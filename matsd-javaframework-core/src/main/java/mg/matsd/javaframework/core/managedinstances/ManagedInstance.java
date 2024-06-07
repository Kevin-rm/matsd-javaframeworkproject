package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ManagedInstance {
    public enum Scope { SINGLETON, PROTOTYPE }

    private String         id;
    private Class<?>       clazz;
    private Scope          scope;
    private final List<Property> properties;

    public ManagedInstance(@Nullable String id, String clazz, @Nullable String scope) {
        this.setClazz(clazz)
            .setId(id)
            .setScope(scope);

        properties = new ArrayList<>();
    }

    public ManagedInstance(@Nullable String id, Class<?> clazz, @Nullable Scope scope) {
        this.setClazz(clazz)
            .setId(id)
            .setScope(scope);

        properties = new ArrayList<>();
    }

    public ManagedInstance(@Nullable String id, Class<?> clazz, @Nullable String scope) {
        this.setClazz(clazz)
            .setId(id)
            .setScope(scope);

        properties = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    private ManagedInstance setId(@Nullable String id) {
        Assert.notBlank(id, true, "L'identifiant d'une \"ManagedInstance\" ne peut pas être vide");

        if (id == null) {
            this.id = clazz.getName();
            return this;
        }

        this.id = id.strip();
        return this;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    private ManagedInstance setClazz(Class<?> clazz) {
        if (!ClassUtils.isPublic(clazz))
            throw new ManagedInstanceCreationException(
                String.format("La classe \"%s\" n'est pas publiquement accessible", clazz.getName())
            );

        this.clazz = clazz;
        return this;
    }

    private ManagedInstance setClazz(String className) {
        Assert.notBlank(className, false, "Le nom de la classe associée à une \"ManagedInstance\" ne peut pas être vide ou \"null\"");

        className = className.strip();
        try {
            return setClazz(Class.forName(className));
        } catch (ClassNotFoundException e) {
            String message = String.format("La classe \"%s\" est introuvable", className);
            if (id != null)
                message += String.format(" pour la \"ManagedInstance\" avec l'identifiant \"%s\"", id);
            throw new ManagedInstanceCreationException(message);
        }
    }

    public Scope getScope() {
        return scope;
    }

    private ManagedInstance setScope(@Nullable Scope scope) {
        if (scope == null) scope = Scope.SINGLETON;

        this.scope = scope;
        return this;
    }

    private ManagedInstance setScope(@Nullable String scope) {
        if (scope == null)              scope = "singleton";
        if (StringUtils.isBlank(scope)) scope = "singleton";

        try {
            return setScope(Scope.valueOf(scope.strip().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ManagedInstanceCreationException(String.format("Scope non valide : %s", scope));
        }
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(String name, String value, String ref) {
        Property property = new Property(name, value, ref, this);

        for (Property p : properties)
            if (property.getField() == p.getField())
                throw new ManagedInstanceCreationException(
                    String.format("La propriété \"%s\" est redondante pour la \"ManagedInstance\" avec l'ID \"%s\"", property.getField().getName(), getId())
                );

        properties.add(property);
    }
}
