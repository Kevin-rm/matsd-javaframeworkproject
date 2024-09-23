package mg.matsd.javaframework.orm.base.internal;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.orm.annotations.*;
import mg.matsd.javaframework.orm.exceptions.BadQueryException;
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

    public static List<Object> retrievePrimaryKeyValue(mg.matsd.javaframework.orm.mapping.Entity entity, String sql, ResultSet resultSet)
        throws SQLException {
        List<Object> results = new ArrayList<>();
        List<String> missingColumns = new ArrayList<>();

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        for (mg.matsd.javaframework.orm.mapping.Column primaryKeyColumn : entity.getPrimaryKey()) {
            boolean found = false;
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
                if (
                    resultSetMetaData.getColumnName(i).equals(primaryKeyColumn.getName()) &&
                    resultSetMetaData.getTableName(i).equals(entity.getTableName())
                ) {
                    results.add(resultSet.getObject(i));
                    found = true;
                    break;
                }

            if (!found) missingColumns.add(primaryKeyColumn.getName());
        }

        if (!missingColumns.isEmpty())
            throw new BadQueryException(String.format("Les colonnes (%s) de la clé primaire de l'entité \"%s\" " +
                "sont soit manquantes, soit incomplètes dans la requête : \"%s\"",
                String.join(", ", missingColumns), entity.getClazz(), sql));

        return results;
    }

    @SuppressWarnings("all")
    public static void eagerFetchToManyRelationships(
        mg.matsd.javaframework.orm.mapping.Entity entity,
        Object instance,
        String sql,
        ResultSet resultSet,
        Map<RelationshipPrimaryKeyValue, Object> toOneInstances
    ) throws SQLException {
        for (Relationship relationship : entity.getToManyRelationships()) {
            Field relationshipField = relationship.getField();
            relationshipField.setAccessible(true);
            try {
                Collection collection = (Collection) relationshipField.get(instance);
                if (collection == null) throw new CollectionNotInitializedException(entity, relationshipField);

                Object targetEntityInstance = hydrateSingleEntity(instance, entity, relationship.getTargetEntity(), sql, resultSet, toOneInstances);
                if (targetEntityInstance == null) continue;

                collection.add(targetEntityInstance);
            } catch (IllegalAccessException ignored) { }
        }
    }

    @Nullable
    public static Object hydrateSingleEntity(
        @Nullable Object instance,
        @Nullable mg.matsd.javaframework.orm.mapping.Entity previous,
        mg.matsd.javaframework.orm.mapping.Entity entity,
        String sql,
        ResultSet resultSet,
        Map<RelationshipPrimaryKeyValue, Object> toOneInstances
    ) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        return hydrateSingleEntity(
            instance, previous, entity, sql, resultSet, resultSetMetaData,
            new int[]{1}, resultSetMetaData.getColumnCount(), new HashMap<>(), toOneInstances
        );
    }

    @Nullable
    private static Object hydrateSingleEntity(
        @Nullable Object instance,
        @Nullable mg.matsd.javaframework.orm.mapping.Entity previous,
        mg.matsd.javaframework.orm.mapping.Entity current,
        String sql,
        ResultSet resultSet,
        ResultSetMetaData resultSetMetaData,
        int[] index,
        int   columnCount,
        Map<String, Relationship> visitedRelationships,
        Map<RelationshipPrimaryKeyValue, Object> toOneInstances
    ) throws SQLException {
        boolean noFieldSet = true;

        for (; index[0] <= columnCount; index[0]++) {
            String columnName = resultSetMetaData.getColumnName(index[0]);
            String tableName  = resultSetMetaData.getTableName(index[0]);
            Class<?> currentClass = current.getClazz();

            if (current.getTableName().equals(tableName) && current.hasColumn(columnName)) {
                instance = instantiateIfNull(instance, currentClass);

                setFieldValue(instance, current.getColumn(columnName).getField(), resultSet, index[0], null);
                noFieldSet = false;
            } else if (hasToOneRelationship(previous, tableName, visitedRelationships)) {
                index[0]--;
                return instance;
            } else {
                Relationship relationship = getToOneRelationship(current, tableName, visitedRelationships);
                if (relationship == null) continue;

                mg.matsd.javaframework.orm.mapping.Entity targetEntity = relationship.getTargetEntity();
                List<Object> targetEntityPrimaryKeyValue = retrievePrimaryKeyValue(targetEntity, sql, resultSet);
                RelationshipPrimaryKeyValue relationshipPrimaryKeyValue = new RelationshipPrimaryKeyValue(relationship, targetEntityPrimaryKeyValue);

                Field relationshipField = relationship.getField();
                relationshipField.setAccessible(true);
                try {
                    Object targetEntityInstance = toOneInstances.get(relationshipPrimaryKeyValue);

                    if (previous == targetEntity) {
                        if (targetEntityInstance != null) continue;
                        targetEntityInstance = instance;
                        toOneInstances.put(relationshipPrimaryKeyValue, targetEntityInstance);

                        instance = instantiate(currentClass);
                        previous = null;
                    } else {
                        if (targetEntityInstance == null) {
                            targetEntityInstance = hydrateSingleEntity(null, current, targetEntity, sql, resultSet, resultSetMetaData, index, columnCount, visitedRelationships, toOneInstances);
                            toOneInstances.put(relationshipPrimaryKeyValue, targetEntityInstance);
                        }
                        instance = instantiateIfNull(instance, currentClass);

                        visitedRelationships.put(tableName, relationship);
                    }

                    relationshipField.set(instance, targetEntityInstance);
                } catch (IllegalAccessException ignored) { }
            }
        }

        return noFieldSet ? null : instance;
    }

    private static Object instantiateIfNull(Object instance, Class<?> clazz) {
        if (instance != null) return instance;

        return instantiate(clazz);
    }

    private static boolean hasToOneRelationship(
        @Nullable mg.matsd.javaframework.orm.mapping.Entity entity, String tableName, Map<String, Relationship> visitedRelationships
    ) {
        return entity != null && getToOneRelationship(entity, tableName, visitedRelationships) != null;
    }

    @Nullable
    private static Relationship getToOneRelationship(
        mg.matsd.javaframework.orm.mapping.Entity entity, String tableName, Map<String, Relationship> visitedRelationships
    ) {
        return !visitedRelationships.isEmpty() && visitedRelationships.containsKey(tableName) ? visitedRelationships.get(tableName) :
            entity.getToOneRelationships().stream()
                .filter(relationship -> relationship.getTargetEntity().getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
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
}
