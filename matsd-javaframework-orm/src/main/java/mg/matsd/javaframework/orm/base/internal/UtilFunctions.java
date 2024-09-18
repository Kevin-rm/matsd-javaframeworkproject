package mg.matsd.javaframework.orm.base.internal;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.orm.annotations.*;
import mg.matsd.javaframework.orm.mapping.MappingException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLOutput;

public final class UtilFunctions {
    private UtilFunctions() { }

    public static boolean isNotEntity(@Nullable Class<?> clazz) {
        if (
            clazz == null || clazz.isAnnotation() || !ClassUtils.isPublic(clazz)
        ) return true;

        return !clazz.isAnnotationPresent(Entity.class);
    }

    public static void assertIsEntity(@Nullable Class<?> clazz) {
        if (isNotEntity(clazz))
            throw new IllegalArgumentException(
                String.format("La classe \"%s\" n'est pas une entité", clazz.getName()));
    }

    public static boolean isRelationshipField(@Nullable Field field) {
        if (field == null) return false;

        return field.isAnnotationPresent(ManyToMany.class) ||
               field.isAnnotationPresent(ManyToOne.class)  ||
               field.isAnnotationPresent(OneToMany.class)  ||
               field.isAnnotationPresent(OneToOne.class);
    }

    public static Object instantiate(Class<?> clazz) throws MappingException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new MappingException(String.format(
                "Pour effectuer correctement le mapping : " +
                    "La classe \"%s\" doit absolument avoir un constructeur sans arguments, " +
                    "non privé et qui ne jette aucune exception", clazz.getName())
            );
        }
    }

    public static void setFieldValue(
        Object instance, Field field, ResultSet resultSet, int columnIndex, @Nullable Class<?> fieldType
    ) throws SQLException {
        field.setAccessible(true);
        try {
            fieldType = fieldType == null ? field.getType() : fieldType;

            Object value = resultSet.getObject(columnIndex, fieldType);
            value = value == null && fieldType.isPrimitive() ? ClassUtils.getPrimitiveDefaultValue(fieldType) : value;
            field.set(instance, value);
        } catch (IllegalAccessException ignored) { }
    }

    @SuppressWarnings("unchecked")
    public static <T> T resultSetRowToObject(Class<T> clazz, ResultSet resultSet) throws SQLException {
        T result;

        if (clazz == Object.class)
            return (T) resultSet.getObject(1);

        if (ClassUtils.isStandardClass(clazz))
            return resultSet.getObject(1, clazz);

        result = (T) instantiate(clazz);

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = resultSetMetaData.getColumnName(i);

            Field field;
            try {
                field = clazz.getDeclaredField(columnName);
            } catch (NoSuchFieldException e) {
                throw new MappingException(
                    String.format("La classe \"%s\" n'a pas de champ nommé \"%s\"", clazz.getName(), columnName)
                );
            }

            Class<?> fieldType = field.getType();
            if (!ClassUtils.isStandardClass(fieldType)) continue;

            setFieldValue(result, field, resultSet, i, fieldType);
        }

        return result;
    }
}
