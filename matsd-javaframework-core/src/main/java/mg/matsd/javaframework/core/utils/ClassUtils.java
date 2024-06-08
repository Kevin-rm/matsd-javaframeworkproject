package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ClassUtils {
    private static final Map<Class<?>, Class<?>> WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP = new IdentityHashMap<>(8);
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_TO_WRAPPER_CLASS_MAP = new IdentityHashMap<>(8);

    static {
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Byte.class, byte.class);
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Character.class, char.class);
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Double.class, double.class);
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Float.class, float.class);
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Integer.class, int.class);
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Long.class, long.class);
        WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.put(Short.class, short.class);

        for (Map.Entry<Class<?>, Class<?>> entry : WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.entrySet())
            PRIMITIVE_TYPE_TO_WRAPPER_CLASS_MAP.put(entry.getValue(), entry.getKey());
    }

    private ClassUtils() { }

    public static boolean isAssignable(Class<?> c1, Class<?> c2) {
        Assert.notNull(c1);
        Assert.notNull(c2);

        if (c1.isAssignableFrom(c2)) return true;
        if (c1.isPrimitive())        return c1 == WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.get(c2);
        else {
            Class<?> wrapper = PRIMITIVE_TYPE_TO_WRAPPER_CLASS_MAP.get(c2);
            return wrapper != null && c1.isAssignableFrom(wrapper);
        }
    }

    public static boolean isAssignable(Class<?> type, @Nullable Object value) {
        Assert.notNull(type, "Type ne peut pas être \"null\"");

        return value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive();
    }

    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "La classe ne peut pas être \"null\"");

        return WRAPPER_CLASS_TO_PRIMITIVE_TYPE_MAP.containsKey(clazz);
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "La classe ne peut pas être \"null\"");

        return clazz.isPrimitive() || isPrimitiveWrapper(clazz);
    }

    public static boolean isPublic(Class<?> clazz) {
        Assert.notNull(clazz, "La classe ne peut pas être \"null\"");

        return Modifier.isPublic(clazz.getModifiers());
    }

    public static boolean isAbstract(Class<?> clazz) {
        Assert.notNull(clazz, "La classe ne peut pas être \"null\"");

        return Modifier.isAbstract(clazz.getModifiers());
    }

    public static boolean isAFieldOfClass(Class<?> clazz, String fieldName) {
        Assert.notNull(clazz, "La classe ne peut pas être \"null\"");
        Assert.notBlank(fieldName, false, "Le nom de l'attribut ne peut pas être vide ou \"null\"");

        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public static boolean isStandardClass(@Nullable Class<?> clazz) {
        if (clazz == null) return false;

        return clazz.getClassLoader() == null;
    }

    public static boolean hasMethod(Class<?> clazz, String methodName) {
        Assert.notNull(clazz, "La classe ne peut pas être \"null\"");
        Assert.notBlank(methodName, false, "Le nom de la méthode ne peut pas être vide ou \"null\"");

        return Arrays.stream(clazz.getDeclaredMethods())
            .anyMatch(
                m -> m.getName().equals(methodName.strip())
            );
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?> ...parameterTypes) throws NoSuchMethodException {
        Assert.notNull(clazz, "La classe ne peut pas être \"null\"");
        Assert.notBlank(methodName, false, "Le nom de la méthode ne peut pas être vide ou \"null\"");

        if (parameterTypes != null)
            for (Class<?> parameterType : parameterTypes)
                Assert.notNull(parameterType, "Chaque type de paramètre ne peut pas être \"null\"");

        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException(String.format("La classe \"%s\" ne possède aucune méthode nommée \"%s\" avec le(s) type(s) de paramètre suivant(s) : %s",
                clazz.getName(), methodName, Arrays.toString(parameterTypes))
            );
        }
    }

    public static Object getPrimitiveDefaultValue(Class<?> primitiveType) {
        Assert.notNull(primitiveType, "L'argument primitiveType ne peut pas être \"null\"");

        if (primitiveType == int.class)          return 0;
        else if (primitiveType == boolean.class) return false;
        else if (primitiveType == long.class)    return 0L;
        else if (primitiveType == double.class)  return 0.0;
        else if (primitiveType == float.class)   return 0.0f;
        else if (primitiveType == char.class)    return '\u0000';
        else if (primitiveType == byte.class)    return (byte) 0;
        else if (primitiveType == short.class)   return (short) 0;

        throw new IllegalArgumentException(String.format(
            "La classe \"%s\" n'est pas une classe primitive", primitiveType.getName()
        ));
    }
}
