package mg.matsd.javaframework.orm.mapping;

import com.sun.jdi.InternalException;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.annotations.ManyToOne;
import mg.matsd.javaframework.orm.annotations.OneToOne;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class Column {
    private Field    field;
    private String   name;
    private boolean  unique    = false;
    private SQLTypes columnDefinition;
    private int      length    = -1;
    private int      precision = -1;
    private int      scale     = -1;
    private boolean  updatable = true;

    Column(Field field) {
        this.field = field;
        this.setName()
            .setUnique()
            .setColumnDefinition()
            .setLength()
            .setPrecision()
            .setScale()
            .setUpdatable();
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    private Column setName() {
        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class))
             name = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).name();
        else if (
            field.isAnnotationPresent(ManyToOne.class) ||
            (field.isAnnotationPresent(OneToOne.class) &&
            !StringUtils.isBlank(field.getAnnotation(OneToOne.class).mappedBy()))
        ) {

        } else name = StringUtils.toSnakeCase(field.getName());

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

    public SQLTypes getColumnDefinition() {
        return columnDefinition;
    }

    private Column setColumnDefinition() {
        Object sqlTypes = SQLTypes.getCorrespondingSqlType(field.getType());
        if (sqlTypes == null)
            throw new TypeMismatchException(String.format("Aucun type de données SQL ne correspond au type \"%s\" du champ \"%s\"",
                field.getType().getName(), field.getName()));

        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
            SQLTypes columnDefinition = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).columnDefinition();
            if (
                !((List<?>) sqlTypes).contains(columnDefinition)
            )
                throw new TypeMismatchException(
                    String.format("Le type de colonne spécifié dans l'annotation (=\"%s\") ne correspond pas au type réel (=\"%s\") pour le champ \"%s\"",
                        columnDefinition, field.getType().getSimpleName(), field.getName()
                    )
                );

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
                 throw new IllegalArgumentException("La taille d'un type de donnée texte ne peut pas être négative");

               length = l;
        } else length = 255;

        return this;
    }

    public int getPrecision() {
        return precision;
    }

    private Column setPrecision() {
        List<?> sqlTypesForDouble = (List<?>) SQLTypes.getCorrespondingSqlType(double.class);
        if (!sqlTypesForDouble.contains(columnDefinition)) return this;

        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
            int p = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).precision();
            if (p < 0)
                throw new IllegalArgumentException("La précision ne peut pas être un nombre négatif");

               precision = p;
        } else precision = 18;

        return this;
    }

    public int getScale() {
        return scale;
    }

    private Column setScale() {
        List<?> sqlTypesForDouble = (List<?>) SQLTypes.getCorrespondingSqlType(double.class);
        if (!sqlTypesForDouble.contains(columnDefinition)) return this;

        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.Column.class)) {
            int s = field.getAnnotation(mg.matsd.javaframework.orm.annotations.Column.class).scale();
            if (s < 0)
                throw new IllegalArgumentException("L'échelle ne peut pas être un nombre négatif");

               scale = s;
        } else scale = 0;

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
            "name='" + name + '\'' +
            ", unique=" + unique +
            ", columnDefinition=" + columnDefinition +
            ", length=" + length +
            ", precision=" + precision +
            ", scale=" + scale +
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
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
