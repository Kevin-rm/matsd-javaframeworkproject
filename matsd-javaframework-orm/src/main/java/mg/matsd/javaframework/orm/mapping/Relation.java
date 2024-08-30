package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.annotations.*;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Relation {
    private Class<?>     entityClass;
    private String       name;
    private Set<Column>  primaryKey;
    private List<Column> columns;

    public Relation(Class<?> entityClass) {
        this.setEntityClass(entityClass)
            .setTableName()
            .setColumns()
            .setPrimaryKey();
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    private Relation setEntityClass(Class<?> entityClass) {
        UtilFunctions.assertIsEntity(entityClass);

        this.entityClass = entityClass;
        return this;
    }

    public String getName() {
        return name;
    }

    public Relation setTableName() {
        if (entityClass.isAnnotationPresent(Table.class)) {
            name = entityClass.getAnnotation(Table.class).name();

            return this;
        }

        name = StringUtils.toSnakeCase(entityClass.getSimpleName());
        return this;
    }

    public Set<Column> getPrimaryKey() {
        return primaryKey;
    }

    private Relation setPrimaryKey() {
        primaryKey = new HashSet<>();

        for (Column column : columns)
            if (column.getField().isAnnotationPresent(PrimaryKey.class))
                primaryKey.add(column);

        if (primaryKey.isEmpty())
            throw new PrimaryKeyNotFoundException(entityClass);

        return this;
    }

    public List<Column> getColumns() {
        return columns;
    }

    private Relation setColumns() {
        columns = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (
                field.isAnnotationPresent(Transient.class)  ||
                field.isAnnotationPresent(ManyToMany.class) ||
                field.isAnnotationPresent(OneToMany.class)
            ) continue;
            columns.add(new Column(field));
        }

        return this;
    }

    public Column getColumn(String name, boolean byFieldName) throws NoSuchColumnException {
        Assert.notBlank(name, false,
            String.format("%s ne peut pas être vide ou \"null\"", (byFieldName ? "Le nom du champ" : "Le nom de colonne"))
        );

        name = name.strip();
        for (Column column : columns) {
            if (byFieldName) {
                if (column.getField().getName().equals(name)) return column;
            }
            else {
                if (column.getName().equals(name))            return column;
            }
        }

        throw new NoSuchColumnException(this, name);
    }

    public Column getColumn(String columnName) throws NoSuchColumnException {
        return getColumn(columnName, false);
    }

    public boolean hasColumn(String name, boolean byFieldName) {
        try {
            getColumn(name, byFieldName);
            return true;
        } catch (NoSuchColumnException e) {
            return false;
        }
    }

    public boolean hasColumn(String columnName) {
        return hasColumn(columnName, false);
    }
}
