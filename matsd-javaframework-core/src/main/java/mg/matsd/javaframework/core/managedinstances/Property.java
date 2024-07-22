package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringConverter;

import java.lang.reflect.Field;

public class Property {
    private Field field;
    private Object value;
    @Nullable
    private String ref;
    private final ManagedInstance managedInstance;

    public Property(String field, String value, String ref, ManagedInstance managedInstance) {
        if (value == null && ref == null)
            throw new InvalidPropertyException(String.format("Ni valeur ni référence n'a été spécifié pour la propriété \"%s\"", field));

        if (value != null && ref != null)
            throw new InvalidPropertyException("Une propriété doit avoir soit une valeur, soit une référence, mais pas les deux en même temps");

        this.managedInstance = managedInstance;
        this.setField(field)
            .setValue(value)
            .setRef  (ref);
    }

    public Field getField() {
        return field;
    }

    private Property setField(Field field) {
        this.field = field;
        return this;
    }

    private Property setField(String fieldName) {
        Assert.notBlank(fieldName, false, "Le nom d'une propriété ne peut pas être vide ou \"null\"");

        fieldName = fieldName.strip();
        try {
            return setField(managedInstance.getClazz().getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new InvalidPropertyException(
                String.format("\"%s\" n'est pas une propriété de la classe \"%s\"", fieldName, managedInstance.getClazz().getName())
            );
        }
    }

    public Object getValue() {
        return value;
    }

    public Property setValue(@Nullable Object value) {
        if (!ClassUtils.isAssignable(field.getType(), value)) {
            throw new TypeMismatchException(
                String.format(
                    "La valeur spécifiée \"%s\" ne correspond pas au type attendu \"%s\" pour la propriété \"%s\"",
                    value, field.getType().getSimpleName(), field.getName()
                )
            );
        }

        this.value = value;
        return this;
    }

    private Property setValue(@Nullable String value) {
        Object obj = null;
        if (value != null && StringUtils.hasText(value)) {
            try {
               obj = StringConverter.convert(value, field.getType());
            } catch (TypeMismatchException e) {
                throw new TypeMismatchException(
                    String.format(
                        "La valeur \"%s\" fournie pour la propriété \"%s\" ne correspond pas au type \"%s\" attendu",
                        value, field.getName(), field.getType().getSimpleName()
                    )
                );
            }
        }

        return setValue(obj);
    }

    public String getRef() {
        return ref;
    }

    private Property setRef(@Nullable String ref) {
        if (ref != null) {
            Assert.notBlank(ref, true, "La référence d'une propriété ne peut pas être vide");
            ref = ref.strip();
        }

        this.ref = ref;
        return this;
    }
}
