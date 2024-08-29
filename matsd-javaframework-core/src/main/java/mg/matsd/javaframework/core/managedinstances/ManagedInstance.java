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
    private String   id;
    private Class<?> clazz;
    private Scope    scope;
    @Nullable
    private ManagedInstance parent;
    @Nullable
    private Method factoryMethod;
    private List<Property> properties;
    private List<ConstructorArgument> constructorArguments;

    public ManagedInstance(
        @Nullable String id,
        Class<?> clazz,
        @Nullable String scope,
        @Nullable ManagedInstance parent,
        @Nullable Method factoryMethod
    ) {
        this.setClazz(clazz)
            .setParent(parent)
            .setFactoryMethod(factoryMethod)
            .setId(id)
            .setScope(scope);

        initPropertiesAndConstructorArguments();
    }

    public ManagedInstance(@Nullable String id, String clazz, @Nullable String scope) {
        this.setClazz(clazz)
            .setId(id)
            .setScope(scope);

        initPropertiesAndConstructorArguments();
    }

    private void initPropertiesAndConstructorArguments() {
        properties = new ArrayList<>();
        constructorArguments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    private ManagedInstance setId(@Nullable String id) {
        Assert.notBlank(id, true, "L'identifiant d'une \"ManagedInstance\" ne peut pas être vide");

        if (id == null) {
            if (factoryMethod != null)
                 this.id = parent.getId() + "." + factoryMethod.getName();
            else this.id = clazz.getName();

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
    public ManagedInstance getParent() {
        return parent;
    }

    private ManagedInstance setParent(@Nullable ManagedInstance parent) {
        this.parent = parent;
        return this;
    }

    @Nullable
    public Method getFactoryMethod() {
        return factoryMethod;
    }

    private ManagedInstance setFactoryMethod(@Nullable Method factoryMethod) {
        if (factoryMethod == null) return this;

        Assert.state(parent != null, "Tentative de définition d'une \"factoryMethod\" à une \"ManagedInstance\" " +
            "alors que celle-ci n'a pas de parent");
        Assert.state(parent.getClazz() == factoryMethod.getDeclaringClass(), String.format(
            "Le parent de la \"ManagedInstance\" avec l'identifiant \"%s\" (\"%s\") ne dispose pas d'une méthode nommée \"%s\"",
            id, parent.getId(), factoryMethod
        ));
        Assert.state(ClassUtils.isAssignable(clazz, factoryMethod.getReturnType()), String.format(
            "Le type de retour de la \"factoryMethod\" \"%s\" doit être assignable " +
            "à la classe associée à la \"ManagedInstance\"", factoryMethod
        ));

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

    public void addConstructorArgument(int index, Class<?> type, @Nullable String reference) {
        addConstructorArgument(new ConstructorArgument(index, type, reference));
    }

    public void addConstructorArgument(
        @Nullable String index,
        @Nullable String value,
        @Nullable String reference,
        Constructor<?> constructor
    ) {
        addConstructorArgument(new ConstructorArgument(
            index, value, reference, generateConstructorArgumentIndex(), constructor
        ));
    }

    private void addConstructorArgument(ConstructorArgument constructorArgument) {
        final int i = constructorArgument.getIndex();
        constructorArguments.stream().filter(c -> i == c.getIndex()).forEachOrdered(c -> {
            throw new ManagedInstanceCreationException(
                String.format("L'argument de constructeur avec l'indice %d " +
                    "est redondant pour la \"ManagedInstance\" avec l'ID \"%s\"", i, id)
            );
        });

        constructorArguments.add(constructorArgument);
    }

    private int generateConstructorArgumentIndex() {
        return constructorArguments.stream()
            .mapToInt(ConstructorArgument::getIndex)
            .filter(constructorArgument -> constructorArgument >= 0)
            .max()
            .orElse(0) + 1;
    }
}
