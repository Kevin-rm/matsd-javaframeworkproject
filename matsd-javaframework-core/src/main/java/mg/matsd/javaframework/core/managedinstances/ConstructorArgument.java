package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.converter.StringConverter;

public class ConstructorArgument {
    private int index;
    private Class<?> type;
    private Object   value;
    @Nullable
    private String   ref;

    public ConstructorArgument(String index, String value, String ref) {
        this.setIndex(index)
            .initValueAndReference(value, ref);
    }

    public ConstructorArgument(int index, String value, String ref) {
        this.setIndex(index)
            .initValueAndReference(value, ref);
    }

    public int getIndex() {
        return index;
    }

    public ConstructorArgument setIndex(int index) {
        Assert.positiveOrZero(index, "L'indice d'un argument de constructeur ne peut pas être négatif");

        this.index = index;
        return this;
    }

    private ConstructorArgument setIndex(String index) {
        try {
            return setIndex(StringConverter.convert(index, Integer.class));
        } catch (TypeMismatchException e) {
            throw new TypeMismatchException(String.format(
                "La valeur de l'indice fournie \"%s\" n'est pas un \"integer\"", index
            ));
        }
    }

    public Class<?> getType() {
        return type;
    }

    ConstructorArgument setType(Class<?> type) {
        this.type = type;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public ConstructorArgument setValue(@Nullable Object value) {
        if (this.value == null || value instanceof String)
            this.value = value;
        else {
            Assert.state(type != null, "Le type de l'argument de constructeur est \"null\"");

            try {
                this.value = StringConverter.convert((String) this.value, type);
            } catch (TypeMismatchException e) {
                throw new TypeMismatchException(String.format(
                    "La valeur fournie pour l'argument de constructeur à l'indice %d ne correspond pas au type \"%s\" attendu",
                    index, type
                ));
            }
        }

        return this;
    }

    public String getRef() {
        return ref;
    }

    private ConstructorArgument setRef(@Nullable String ref) {
        if (ref != null) {
            Assert.notBlank(ref, true, "La référence d'un argument de constructeur ne peut pas être vide");
            ref = ref.strip();
        }

        this.ref = ref;
        return this;
    }

    private void initValueAndReference(String value, String ref) {
        if (value == null && ref == null)
            throw new ManagedInstanceCreationException(String.format("La valeur et la référence ne peuvent pas être toutes les deux \"null\" " +
                "pour l'argument du constructeur à l'indice %d", this.index)
            );

        if (value != null && ref != null)
            throw new ManagedInstanceCreationException(String.format("Un argument de constructeur doit avoir soit une valeur, " +
                "soit une référence, mais pas les deux en même temps. Indice : %d", this.index)
            );

        this.setValue(value)
            .setRef(ref);
    }
}
