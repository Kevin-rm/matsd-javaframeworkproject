package mg.matsd.javaframework.core.utils.converter;

import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.Set;

public final class StringConverter {
    private static final Set<String> TRUE_VALUES  = Set.of("true", "yes", "1");
    private static final Set<String> FALSE_VALUES = Set.of("false", "no", "0");

    private StringConverter() { }

    public static  <T> T convert(String value, Class<T> type) throws TypeMismatchException {
        Assert.notNull(type, "L'argument type ne peut pas être \"null\"");
        validateArgument(value);

        value = value.strip();
        T result;
        try {
            if (type == String.class)
                return (T) value;
            else if (type == int.class || type == Integer.class)
                result = (T) Integer.valueOf(value);
            else if (type == double.class || type == Double.class)
                result = (T) Double.valueOf(value);
            else if (type == float.class || type == Float.class)
                result = (T) Float.valueOf(value);
            else if (type == boolean.class || type == Boolean.class)
                result = (T) toBoolean(value);
            else throw new UnsupportedConversionTypeException(type);
        } catch (IllegalArgumentException e) {
            throw new TypeMismatchException(String.format(
                "La chaîne de caratères \"%s\" ne peut pas être convertie en \"%s\"", value, type.getName()
            ));
        }

        return result;
    }

    private static Boolean toBoolean(String value) {
        value = value.toLowerCase();
        if (TRUE_VALUES.contains(value))
            return Boolean.TRUE;
        else if (FALSE_VALUES.contains(value))
            return Boolean.FALSE;

        throw new IllegalArgumentException();
    }

    private static void validateArgument(String value) {
        Assert.notBlank(value, false, "La chaîne de caractère à convertir ne peut pas être vide ou \"null\"");
    }
}
