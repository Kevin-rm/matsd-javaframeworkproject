package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.annotations.PrimaryKey;
import mg.matsd.javaframework.orm.annotations.Table;
import mg.matsd.javaframework.orm.annotations.Transient;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;
import mg.matsd.javaframework.orm.setup.SessionFactoryOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Entity {
    private final SessionFactoryOptions sessionFactoryOptions;
    private Class<?>     clazz;
    private String       tableName;
    private List<Column> primaryKey;
    private List<Column> columns;
    private List<Relationship> relationships;

    public Entity(Class<?> clazz, SessionFactoryOptions sessionFactoryOptions) {
        Assert.notNull(sessionFactoryOptions, "L'argument sessionFactoryOptions ne peut pas être \"null\"");

        this.sessionFactoryOptions = sessionFactoryOptions;
        this.setClazz(clazz)
            .setTableName()
            .setColumns()
            .setPrimaryKey();
    }

    public SessionFactoryOptions getSessionFactoryOptions() {
        return sessionFactoryOptions;
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

    public List<Column> getPrimaryKey() {
        return primaryKey;
    }

    private Entity setPrimaryKey() {
        primaryKey = new ArrayList<>();

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

    public void setRelationships() {
        relationships = new ArrayList<>();
        Arrays.stream(clazz.getDeclaredFields())
            .filter(UtilFunctions::isRelationshipField)
            .forEachOrdered(field -> relationships.add(new Relationship(this, field)));
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

    public boolean hasLazyRelationship() {
        return relationships.stream().anyMatch(relationship -> relationship.getFetchType() == FetchType.LAZY);
    }

    public List<Relationship> getToManyRelationships() {
        return relationships.stream().filter(Relationship::isToMany).collect(Collectors.toList());
    }

    public List<Relationship> getToOneRelationships() {
        return relationships.stream().filter(Relationship::isToOne).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return Objects.equals(clazz, entity.clazz);
    }

    @Override
    public int hashCode() {
        return clazz != null ? clazz.hashCode() : 0;
    }
}
