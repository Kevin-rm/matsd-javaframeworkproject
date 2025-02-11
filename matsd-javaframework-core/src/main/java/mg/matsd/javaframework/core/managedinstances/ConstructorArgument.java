package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringConverter;

import java.lang.reflect.Constructor;

public class ConstructorArgument {
    private int index;
    private Class<?> type;
    @Nullable
    private Object value;
    @Nullable
    private String reference;
    private final ManagedInstance managedInstance;

    ConstructorArgument(int index, Class<?> type, @Nullable String reference, ManagedInstance managedInstance) {
        this.managedInstance = managedInstance;
        this.setIndex(index, null)
            .setType(type)
            .setReference(reference);
    }

    ConstructorArgument(
        @Nullable String index,
        @Nullable String value,
        @Nullable String reference,
        Constructor<?> constructor,
        ManagedInstance managedInstance
    ) {
        this.managedInstance = managedInstance;
        this.setIndex(index, constructor)
            .setType(constructor.getParameterTypes()[this.index])
            .initValueAndReference(value, reference);
    }

    private void initValueAndReference(@Nullable String value, @Nullable String reference) {
        if (value == null && reference == null)
            throw new ManagedInstanceCreationException(String.format("La valeur et la référence ne peuvent pas être toutes les deux \"null\" " +
                "pour l'argument du constructeur à l'indice %d", this.index));

        if (value != null && reference != null)
            throw new ManagedInstanceCreationException(String.format("Un argument de constructeur doit avoir soit une valeur, " +
                "soit une référence, mais pas les deux en même temps. Indice : %d", this.index));

        this.setValue(value)
            .setReference(reference);
    }

    public int getIndex() {
        return index;
    }

    private ConstructorArgument setIndex(int index, @Nullable Constructor<?> constructor) {
        Assert.positiveOrZero(index, "L'indice d'un argument de constructeur ne peut pas être négatif");

        if (constructor != null) {
            int maximumIndex = constructor.getParameterCount() - 1;
            if (index > maximumIndex)
                throw new IllegalArgumentException(String.format("L'indice de l'argument de constructeur donné (=%d) dépasse " +
                    "la valeur maximum d'indice (=%d) du constructeur \"%s\"", index, maximumIndex, constructor.getName()));
        }

        this.index = index;
        return this;
    }

    private ConstructorArgument setIndex(String index, @Nullable Constructor<?> constructor) {
        try {
            return setIndex(
                index == null ? managedInstance.generateConstructorArgumentIndex() : StringConverter.convert(index, Integer.class),
                constructor);
        } catch (TypeMismatchException e) {
            throw new TypeMismatchException(String.format(
                "La valeur de l'indice fournie \"%s\" n'est pas un \"integer\"", index
            ));
        }
    }

    public Class<?> getType() {
        return type;
    }

    private ConstructorArgument setType(Class<?> type) {
        Assert.notNull(type, "Le type d'un argument de constructeur ne peut pas être \"null\"");

        this.type = type;
        return this;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public ConstructorArgument setValue(@Nullable Object value) {
        if (!ClassUtils.isAssignable(type, value))
            throw new TypeMismatchException(String.format(
                "La valeur spécifiée \"%s\" ne correspond pas au type attendu \"%s\" pour l'argument de constructeur à l'indice %d",
                value, type.getSimpleName(), index
            ));

        this.value = value;
        return this;
    }

    private ConstructorArgument setValue(@Nullable String value) {
        Object obj = null;
        if (value != null && StringUtils.hasText(value)) try {
            obj = StringConverter.convert(value, type);
        } catch (TypeMismatchException e) {
            throw new TypeMismatchException(String.format(
                "La valeur fournie pour l'argument de constructeur à l'indice %d ne correspond pas au type \"%s\" attendu",
                index, type
            ));
        }

        return setValue(obj);
    }

    public String getReference() {
        return reference;
    }

    private void setReference(@Nullable String reference) {
        if (reference != null) {
            Assert.notBlank(reference, true, "La référence d'un argument de constructeur ne peut pas être vide");
            reference = reference.strip();
        }

        this.reference = reference;
    }
}
