package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class Assert {

    public static void notNull(@Nullable Object object, @Nullable String exceptionMessage) {
        if (object == null)
            throw new NullPointerException(exceptionMessage);
    }

    public static void notNull(@Nullable Object object) {
        notNull(object, null);
    }

    public static void notBlank(String string, boolean allowNull, @Nullable String exceptionMessage) {
        String message = exceptionMessage == null ? "La chaîne de caractères passée en argument est vide" : exceptionMessage;

        if (string == null) {
            if (!allowNull) throw new IllegalArgumentException(message);
            return;
        }
        if (StringUtils.isBlank(string)) throw new IllegalArgumentException(message);
    }

    public static void notBlank(String string, boolean allowNull) {
        notBlank(string, allowNull, null);
    }

    public static void notEmpty(@Nullable Object[] array, @Nullable String exceptionMessage) {
        if (ArrayUtils.isEmpty(array))
            throw new IllegalArgumentException(exceptionMessage == null ?
                "Le tableau passé en argument ne peut pas être vide ou \"null\"" : exceptionMessage);
    }

    public static void noNullElements(@Nullable Object[] array, @Nullable String exceptionMessage) {
        if (array == null) return;

        final String message = exceptionMessage == null ? "Chaque élément du tableau ne peut être \"null\"" : exceptionMessage;
        Arrays.stream(array).filter(Objects::isNull).forEachOrdered(element -> {
            throw new IllegalArgumentException(message);
        });
    }

    public static void state(boolean expression, @Nullable Supplier<? extends RuntimeException> runtimeExceptionSupplier) {
        if (!expression)
            throw nullSafeGet(runtimeExceptionSupplier);
    }

    public static void state(boolean expression, @Nullable String exceptionMessageIfFalse) {
        state(expression, () -> new IllegalStateException(exceptionMessageIfFalse));
    }

    public static void state(boolean expression) {
        state(expression, (String) null);
    }

    public static void positive(int number, @Nullable String exceptionMessage) {
        if (number <= 0)
            throw new IllegalArgumentException(
                exceptionMessage == null ? String.format("Le nombre \"%d\" n'est pas un nombre strictement positif", number) : exceptionMessage
            );
    }

    public static void positiveOrZero(int number, @Nullable String exceptionMessage) {
        if (number < 0)
            throw new IllegalArgumentException(
                exceptionMessage == null ? String.format("Le nombre \"%d\" n'est pas un nombre positif ou nul", number) : exceptionMessage
            );
    }

    public static void negative(int number, @Nullable String exceptionMessage) {
        if (number >= 0)
            throw new IllegalArgumentException(
                exceptionMessage == null ? String.format("Le nombre \"%d\" n'est pas un nombre strictement négatif", number) : exceptionMessage
            );
    }

    public static void negativeOrZero(int number, @Nullable String exceptionMessage) {
        if (number > 0)
            throw new IllegalArgumentException(
                exceptionMessage == null ? String.format("Le nombre \"%d\" n'est pas un nombre négatif ou nul", number) : exceptionMessage
            );
    }

    public static void inRange(int number, int min, int max, @Nullable String exceptionMessage) {
        if (number < min || number > max)
            throw new IllegalArgumentException(
                exceptionMessage == null ? String.format("Le nombre \"%d\" n'est pas compris entre \"%d\" et \"%d\"", number, min, max) : exceptionMessage
            );
    }

    private static RuntimeException nullSafeGet(Supplier<? extends RuntimeException> runtimeExceptionSupplier) {
        if (runtimeExceptionSupplier == null)
            return new IllegalStateException();

        return runtimeExceptionSupplier.get();
    }
}
