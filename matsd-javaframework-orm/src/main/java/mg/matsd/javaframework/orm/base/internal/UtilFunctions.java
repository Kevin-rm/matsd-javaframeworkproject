package mg.matsd.javaframework.orm.base.internal;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.orm.annotations.*;
import mg.matsd.javaframework.orm.exceptions.BadQueryException;
import mg.matsd.javaframework.orm.mapping.FetchType;
import mg.matsd.javaframework.orm.mapping.MappingException;
import mg.matsd.javaframework.orm.mapping.Relationship;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

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

    public static List<Object> retrievePrimaryKeyValue(mg.matsd.javaframework.orm.mapping.Entity entity, String sql, ResultSet resultSet) throws SQLException {
        List<Object> results = new ArrayList<>();
        List<String> missingColumns = new ArrayList<>();

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        for (mg.matsd.javaframework.orm.mapping.Column primaryKeyColumn : entity.getPrimaryKey()) {
            boolean found = false;
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                if (
                    resultSetMetaData.getColumnName(i).equals(primaryKeyColumn.getName()) &&
                    resultSetMetaData.getTableName(i).equals(entity.getTableName())
                ) {
                    results.add(resultSet.getObject(i));
                    found = true;
                    break;
                }
            }

            if (!found) missingColumns.add(primaryKeyColumn.getName());
        }

        if (!missingColumns.isEmpty())
            throw new BadQueryException(String.format("Les valeurs des colonnes de la clé primaire (%s) de l'entité \"%s\" " +
                "n'ont pas été précisées ou sont incomplètes dans la requête \"%s\"",
                String.join(", ", missingColumns), entity.getClazz(), sql));

        return results;
    }

    public static Object hydrateSingleEntity(mg.matsd.javaframework.orm.mapping.Entity entity, ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        return hydrateSingleEntity(
            null, null, entity, resultSet, resultSetMetaData, new int[]{1}, resultSetMetaData.getColumnCount(), new HashMap<>()
        );
    }

    @SuppressWarnings("all")
    public static void fecthEagerToManyRelationships(mg.matsd.javaframework.orm.mapping.Entity entity, Object instance, ResultSet resultSet)
        throws SQLException {
        for (Relationship relationship : entity.getToManyRelationships()) {;
            Field relationshipField = relationship.getField();
            relationshipField.setAccessible(true);

            try {
                Collection collection = (Collection) relationshipField.get(instance);
                if (collection == null) {
                    collection = new ArrayList<>();
                    relationshipField.set(instance, collection);
                }

                collection.add(hydrateSingleEntity(relationship.getTargetEntity(), resultSet));
            } catch (IllegalAccessException ignored) { }
        }
    }

    private static void setFieldValue(
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

    private static Object instantiateIfNull(Object instance, Class<?> clazz) {
        if (instance != null) return instance;

        return instantiate(clazz);
    }

    private static boolean hasToOneRelationship(mg.matsd.javaframework.orm.mapping.Entity entity, String tableName) {
        return entity.getToOneRelationships().stream()
            .anyMatch(relationship ->
                relationship.getTargetEntity().getTableName().equals(tableName)
            );
    }

    private static Relationship getRelationshipByTableName(mg.matsd.javaframework.orm.mapping.Entity current, String tableName, Map<String, Relationship> visitedRelationships) {
        if (visitedRelationships.containsKey(tableName)) return visitedRelationships.get(tableName);

        return current.getRelationships().stream()
            .filter(relationship -> relationship.getTargetEntity().getTableName().equals(tableName))
            .findFirst()
            .orElse(null);
    }

    private static Object hydrateSingleEntity(
        @Nullable Object instance,
        @Nullable mg.matsd.javaframework.orm.mapping.Entity previous,
        mg.matsd.javaframework.orm.mapping.Entity current,
        ResultSet resultSet,
        ResultSetMetaData resultSetMetaData,
        int[] index,
        int   columnCount,
        Map<String, Relationship> visitedRelationships
    ) throws SQLException {
        for (; index[0] <= columnCount; index[0]++) {
            String columnName = resultSetMetaData.getColumnName(index[0]);
            String tableName  = resultSetMetaData.getTableName(index[0]);

            if (current.getTableName().equals(tableName) && current.hasColumn(columnName)) {
                instance = instantiateIfNull(instance, current.getClazz());

                setFieldValue(instance, current.getColumn(columnName).getField(), resultSet, index[0], null);
            } else {
                if (previous != null && hasToOneRelationship(previous, tableName)) {
                    index[0]--;
                    return instance;
                }

                Relationship relationship = getRelationshipByTableName(current, tableName, visitedRelationships);
                if (relationship == null || relationship.getFetchType() != FetchType.EAGER || relationship.isToMany()) continue;

                mg.matsd.javaframework.orm.mapping.Entity targetEntity = relationship.getTargetEntity();
                if (previous == targetEntity) {
                    index[0] --;
                    return instance;
                }

                Field relationshipField = relationship.getField();
                relationshipField.setAccessible(true);
                try {
                    instance = instantiateIfNull(instance, current.getClazz());

                    Object fieldValue = relationshipField.get(instance);
                    fieldValue = hydrateSingleEntity(fieldValue, current, targetEntity, resultSet, resultSetMetaData, index, columnCount, visitedRelationships);

                    relationshipField.set(instance, fieldValue);
                } catch (IllegalAccessException ignored) { }

                visitedRelationships.put(tableName, relationship);
            }
        }

        return instance;
    }
}
