package mg.matsd.javaframework.di.managedinstances;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.core.utils.converter.StringToTypeConverter;

import java.lang.reflect.Field;

public class Property {
    private Field  field;
    @Nullable
    private Object value;
    @Nullable
    private String reference;
    private final ManagedInstance managedInstance;

    Property(String field, String value, String reference, ManagedInstance managedInstance) {
        if (value == null && reference == null)
            throw new InvalidPropertyException(String.format("Ni valeur ni référence n'a été spécifié pour la propriété \"%s\"", field));

        if (value != null && reference != null)
            throw new InvalidPropertyException("Une propriété doit avoir soit une valeur, soit une référence, mais pas les deux en même temps");

        this.managedInstance = managedInstance;
        this.setField(field)
            .setValue(value)
            .setReference(reference);
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
        final Class<?> c = managedInstance.getClazz();
        try {
            return setField(c.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new InvalidPropertyException(String.format("\"%s\" n'est pas une propriété de la classe \"%s\"",
                fieldName, c.getName()
            ));
        }
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public Property setValue(@Nullable Object value) {
        final Class<?> fieldType = field.getType();
        if (!ClassUtils.isAssignable(fieldType, value)) throw new TypeMismatchException(
            String.format("La valeur spécifiée \"%s\" ne correspond pas au type attendu \"%s\" pour la propriété \"%s\"",
                value, fieldType.getSimpleName(), field.getName()
            ));

        this.value = value;
        return this;
    }

    private Property setValue(@Nullable String value) {
        Object obj = null;
        final Class<?> fieldType = field.getType();
        if (value != null && StringUtils.hasText(value)) try {
            obj = StringToTypeConverter.convert(value, fieldType);
        } catch (TypeMismatchException e) {
            throw new TypeMismatchException(String.format(
                "La valeur \"%s\" fournie pour la propriété \"%s\" ne correspond pas au type \"%s\" attendu",
                value, field.getName(), fieldType.getSimpleName()
            ));
        }

        return setValue(obj);
    }

    public String getReference() {
        return reference;
    }

    private Property setReference(@Nullable String reference) {
        if (reference != null) {
            Assert.notBlank(reference, true, "La référence d'une propriété ne peut pas être vide");
            reference = reference.strip();
        }

        this.reference = reference;
        return this;
    }
}
