package mg.matsd.javaframework.orm.mapping;

import com.sun.jdi.InternalException;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class Column {
    private final Entity entity;
    private final Field  field;
    private String   name;
    private SQLTypes columnDefinition;
    private int      length    = -1;
    private int      precision = -1;
    private int      scale     = -1;
    private boolean  unique    = false;
    private boolean  updatable = true;

    Column(Field field, Entity entity) {
        this.entity = entity;
        this.field  = field;
        this.setName()
            .setColumnDefinition()
            .setLength()
            .setPrecision()
            .setScale()
            .setUnique()
            .setUpdatable();
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    private Column setName() {
        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
            name = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).name();

            if (StringUtils.isBlank(name))
                throw new MappingException("Le nom de colonne d'une entité ne peut pas être vide");
        } else name = StringUtils.toSnakeCase(field.getName());

        return this;
    }

    public SQLTypes getColumnDefinition() {
        return columnDefinition;
    }

    private Column setColumnDefinition() {
        Object sqlTypes = SQLTypes.getCorrespondingSqlType(field.getType());
        if (sqlTypes == null)
            throw new MappingException(new TypeMismatchException(
                String.format("Aucun type de données SQL ne correspond au type \"%s\" du champ \"%s\" de l'entité \"%s\"",
                field.getType().getName(), field.getName(), entity.getClazz().getName())
            ));

        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
            SQLTypes columnDefinition = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).columnDefinition();
            if (
                (sqlTypes instanceof SQLTypes type && type != columnDefinition) ||
                (sqlTypes instanceof List<?> list && !list.contains(columnDefinition))
            )
                throw new MappingException(new TypeMismatchException(
                    String.format("Le type de colonne spécifié dans l'annotation (=\"%s\") ne correspond pas " +
                        "au type réel (=\"%s\") pour le champ \"%s\" de l'entité \"%s\"",
                    columnDefinition, field.getType().getSimpleName(), field.getName(), entity.getClazz().getName())
                ));

            this.columnDefinition = columnDefinition;
        } else {
            if (sqlTypes instanceof SQLTypes)
                this.columnDefinition = (SQLTypes) sqlTypes;
            else if (sqlTypes instanceof List<?>)
                this.columnDefinition = (SQLTypes) ((List<?>) sqlTypes).get(0);
            else throw new InternalException();
        }

        return this;
    }

    public int getLength() {
        return length;
    }

    private Column setLength() {
        if (columnDefinition != SQLTypes.VARCHAR) return this;

        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
             int l = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).length();
             if (l < 0)
                 throw new MappingException("La taille d'un type de donnée texte ne peut pas être négative");

               length = l;
        } else length = 255;

        return this;
    }

    public int getPrecision() {
        return precision;
    }

    @SuppressWarnings("all")
    private Column setPrecision() {
        List<SQLTypes> sqlTypesForDouble = (List<SQLTypes>) SQLTypes.getCorrespondingSqlType(double.class);
        if (!sqlTypesForDouble.contains(columnDefinition)) return this;

        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
            int p = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).precision();
            if (p < 0)
                throw new MappingException("La précision ne peut pas être un nombre négatif");

               precision = p;
        } else precision = 18;

        return this;
    }

    public int getScale() {
        return scale;
    }

    @SuppressWarnings("all")
    private Column setScale() {
        List<SQLTypes> sqlTypesForDouble = (List<SQLTypes>) SQLTypes.getCorrespondingSqlType(double.class);
        if (!sqlTypesForDouble.contains(columnDefinition)) return this;

        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
            int s = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).scale();
            if (s < 0)
                throw new MappingException("L'échelle ne peut pas être un nombre négatif");

               scale = s;
        } else scale = 0;

        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    private Column setUnique() {
        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class))
            unique = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).unique();

        return this;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    private Column setUpdatable() {
        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class))
             updatable = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).updatable();

        return this;
    }

    @Override
    public String toString() {
        return "Column{" +
            "entity=" + entity +
            ", name='" + name + '\'' +
            ", columnDefinition=" + columnDefinition +
            ", length=" + length +
            ", precision=" + precision +
            ", scale=" + scale +
            ", unique=" + unique +
            ", updatable=" + updatable +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        if (!Objects.equals(field, column.field)) return false;
        return Objects.equals(name, column.name);
    }

    @Override
    public int hashCode() {
        int result = entity.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
