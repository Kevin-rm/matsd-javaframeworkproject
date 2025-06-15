package mg.matsd.javaframework.core.utils.converter;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.Assert;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class StringToTypeConverter {
    private static final Set<String> TRUE_VALUES  = Set.of("true", "yes", "1");
    private static final Set<String> FALSE_VALUES = Set.of("false", "no", "0");
    private static final Map<Class<?>, Function<String, ?>> CONVERTERS;

    @Nullable
    private static Set<Class<?>> supportedTypesCache;

    static {
        CONVERTERS = new ConcurrentHashMap<>();

        registerConverter(String.class,        Function.identity());
        registerConverter(int.class,           Integer::valueOf);
        registerConverter(Integer.class,       Integer::valueOf);
        registerConverter(long.class,          Long::valueOf);
        registerConverter(Long.class,          Long::valueOf);
        registerConverter(double.class,        Double::valueOf);
        registerConverter(Double.class,        Double::valueOf);
        registerConverter(float.class,         Float::valueOf);
        registerConverter(Float.class,         Float::valueOf);
        registerConverter(boolean.class,       StringToTypeConverter::toBoolean);
        registerConverter(Boolean.class,       StringToTypeConverter::toBoolean);
        registerConverter(LocalTime.class,     LocalTime::parse);
        registerConverter(LocalDate.class,     LocalDate::parse);
        registerConverter(LocalDateTime.class, LocalDateTime::parse);
        registerConverter(Time.class,          Time::valueOf);
        registerConverter(Date.class,          Date::valueOf);
        registerConverter(Timestamp.class,     Timestamp::valueOf);
        registerConverter(BigDecimal.class,    BigDecimal::new);
    }

    private StringToTypeConverter() { }

    @SuppressWarnings("unchecked")
    public static <T> T convert(String value, Class<T> type) throws TypeMismatchException {
        Assert.notNull(type, "L'argument type ne peut pas être \"null\"");
        Assert.notBlank(value, false, "La chaîne de caractère à convertir ne peut pas être vide ou \"null\"");

        @Nullable final Function<String, ?> converter = CONVERTERS.get(type);
        if (converter == null)
            throw new UnsupportedTypeConversionException(type);

        try {
            return (T) converter.apply(value.strip());
        } catch (IllegalArgumentException e) {
            throw new TypeMismatchException(String.format(
                "La chaîne de caractères \"%s\" ne peut pas être convertie en \"%s\"", value, type.getName()
            ));
        }
    }

    public static <T> void registerConverter(Class<T> type, Function<String, T> converter) {
        Assert.notNull(type, "Le type ne peut pas être \"null\"");
        Assert.notNull(converter, "La fonction de conversion ne peut pas être \"null\"");

        CONVERTERS.put(type, converter);
        invalidateSupportedTypesCache();
    }

    public static void unregisterConverter(Class<?> type) {
        Assert.isTrue(isTypeSupported(type), "Impossible de supprimer car aucun convertisseur n'a été enregistré pour le type : " + type.getName());

        CONVERTERS.remove(type);
        invalidateSupportedTypesCache();
    }

    public static boolean isTypeSupported(Class<?> type) {
        Assert.notNull(type, "Le type ne peut pas être \"null\"");
        return CONVERTERS.containsKey(type);
    }

    public static Set<Class<?>> getSupportedTypes() {
        if (supportedTypesCache != null) return supportedTypesCache;

        return supportedTypesCache = Set.copyOf(CONVERTERS.keySet());
    }

    private static Boolean toBoolean(String value) {
        value = value.toLowerCase();
        if (TRUE_VALUES.contains(value))
            return Boolean.TRUE;
        else if (FALSE_VALUES.contains(value))
            return Boolean.FALSE;

        throw new IllegalArgumentException();
    }

    private static void invalidateSupportedTypesCache() {
        if (supportedTypesCache == null) return;
        supportedTypesCache = null;
    }
}
