package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ManagedInstance {
    public enum Scope { SINGLETON, PROTOTYPE }

    private String         id;
    private Class<?>       clazz;
    private Scope          scope;
    @Nullable
    private Method         factoryMethod;
    private final List<Property> properties;
    private final List<ConstructorArgument> constructorArguments;

    public ManagedInstance(@Nullable String id, String clazz, @Nullable String scope) {
        this.setClazz(clazz)
            .setId(id)
            .setScope(scope);

        properties = new ArrayList<>();
        constructorArguments = new ArrayList<>();
    }

    public ManagedInstance(@Nullable String id, Class<?> clazz, @Nullable Scope scope) {
        this.setClazz(clazz)
            .setId(id)
            .setScope(scope);

        properties = new ArrayList<>();
        constructorArguments = new ArrayList<>();
    }

    public ManagedInstance(@Nullable String id, Class<?> clazz, @Nullable String scope, @Nullable Method factoryMethod) {
        this.setClazz(clazz)
            .setId(id)
            .setScope(scope)
            .setFactoryMethod(factoryMethod);

        properties = new ArrayList<>();
        constructorArguments = new ArrayList<>();
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
        if (scope == null || StringUtils.isBlank(scope))
            scope = "singleton";

        try {
            return setScope(Scope.valueOf(scope.strip().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ManagedInstanceCreationException(String.format("Scope non valide : %s", scope));
        }
    }

    @Nullable
    public Method getFactoryMethod() {
        return factoryMethod;
    }

    private ManagedInstance setFactoryMethod(@Nullable Method factoryMethod) {
        if (factoryMethod == null) return this;

        Assert.state(ClassUtils.isAssignable(clazz, factoryMethod.getReturnType()),
            "Le type de retour de la méthode \"%s\" doit être assignable à la classe associée à la \"ManagedInstance\""
        );
        this.factoryMethod = factoryMethod;
        return this;
    }

    private ManagedInstance setFactoryMethod(@Nullable String factoryMethod) {
        if (factoryMethod == null || StringUtils.isBlank(factoryMethod))
            return this;

        try {
            return setFactoryMethod(ClassUtils.findMethod(clazz, factoryMethod));
        } catch (NoSuchMethodException e) {
            throw new ManagedInstanceCreationException(e);
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
                    String.format("La propriété \"%s\" est redondante pour la \"ManagedInstance\" avec l'ID \"%s\"", property.getField().getName(), id)
                );

        properties.add(property);
    }

    public List<ConstructorArgument> getConstructorArguments() {
        return constructorArguments;
    }

    public void addConstructorArgument(String index, String value, String ref) {
        ConstructorArgument constructorArgument;
        if (index == null || StringUtils.isBlank(index))
             constructorArgument = new ConstructorArgument(maximumConstructorArgumentIndex() + 1, value, ref);
        else constructorArgument = new ConstructorArgument(index, value, ref);

        constructorArguments.stream().filter(c -> constructorArgument.getIndex() == c.getIndex()).forEachOrdered(c -> {
            throw new ManagedInstanceCreationException(
                String.format("L'argument de constructeur avec l'indice %d " +
                    "est redondant pour la \"ManagedInstance\" avec l'ID \"%s\"", constructorArgument.getIndex(), id)
            );
        });
        constructorArguments.add(constructorArgument);
    }

    @Nullable
    Constructor<?> findSuitableConstructor() throws NoSuchMethodException {
        if (constructorArguments.isEmpty())
            return clazz.getConstructor();

        for (Constructor<?> constructor : clazz.getConstructors()) {
            int parameterCount = constructor.getParameterCount();
            if (
                constructorArguments.size()      != parameterCount ||
                maximumConstructorArgumentIndex() > parameterCount - 1
            ) continue;

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                ConstructorArgument constructorArgument = getConstructorArgumentByIndex(i);
                if (constructorArgument == null) continue;

                constructorArgument.setType(parameterTypes[i]);
            }

            return constructor;
        }

        return null;
    }

    private int maximumConstructorArgumentIndex() {
        if (constructorArguments.isEmpty()) return -1;

        return constructorArguments.stream()
            .mapToInt(ConstructorArgument::getIndex)
            .filter(constructorArgument -> constructorArgument >= -1)
            .max()
            .orElse(-1);
    }

    @Nullable
    private ConstructorArgument getConstructorArgumentByIndex(int index) {
        return constructorArguments.stream()
            .filter(constructorArgument -> index == constructorArgument.getIndex())
            .findFirst()
            .orElse(null);
    }
}
