package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.annotations.PrimaryKey;
import mg.matsd.javaframework.orm.annotations.Table;
import mg.matsd.javaframework.orm.annotations.Transient;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;

import java.util.*;

public class Entity {
    private Class<?>     clazz;
    private String       tableName;
    private Set<Column>  primaryKey;
    private List<Column> columns;
    private List<Relationship> relationships;

    public Entity(Class<?> clazz) {
        this.setClazz(clazz)
            .setTableName()
            .setColumns()
            .setPrimaryKey()
            .setRelationships();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    private Entity setClazz(Class<?> clazz) {
        UtilFunctions.assertIsEntity(clazz);

        this.clazz = clazz;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    private Entity setTableName() {
        if (clazz.isAnnotationPresent(Table.class)) {
            tableName = clazz.getAnnotation(Table.class).name();
            if (StringUtils.isBlank(tableName))
                throw new MappingException("Le nom de la table d'une entité ne peut pas être vide");

            return this;
        }

        tableName = StringUtils.toSnakeCase(clazz.getSimpleName());
        return this;
    }

    public Set<Column> getPrimaryKey() {
        return primaryKey;
    }

    private Entity setPrimaryKey() {
        primaryKey = new HashSet<>();

        columns.stream()
            .filter(column -> column.getField().isAnnotationPresent(PrimaryKey.class))
            .forEachOrdered(column -> primaryKey.add(column));

        if (primaryKey.isEmpty())
            throw new MissingPrimaryKeyException(this);

        return this;
    }

    public List<Column> getColumns() {
        return columns;
    }

    private Entity setColumns() {
        columns = new ArrayList<>();
        Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> !field.isAnnotationPresent(Transient.class) && !UtilFunctions.isRelationshipField(field))
            .forEachOrdered(field -> columns.add(new Column(field, this)));

        return this;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    private Entity setRelationships() {
        relationships = new ArrayList<>();
        Arrays.stream(clazz.getDeclaredFields())
            .filter(UtilFunctions::isRelationshipField)
            .forEachOrdered(field -> relationships.add(new Relationship(this, field)));

        return this;
    }

    public Column getColumn(String name, boolean byFieldName) throws NoSuchColumnException {
        Assert.notBlank(name, false, String.format("%s ne peut pas être vide ou \"null\"",
            (byFieldName ? "Le nom du champ" : "Le nom de colonne")));

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
