package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;

public class JoinColumn {
    private final Relationship relationship;
    @Nullable
    private final mg.matsd.javaframework.orm.annotations.JoinColumn joinColumn;
    private String  name;
    private Column  referencedColumn;
    private boolean nullable  = true;
    private boolean unique    = false;
    private boolean updatable = true;

    JoinColumn(Relationship relationship, @Nullable mg.matsd.javaframework.orm.annotations.JoinColumn joinColumn) {
        this.relationship = relationship;
        this.joinColumn   = joinColumn;
        this.setName()
            .setReferencedColumn()
            .setNullable()
            .setUnique()
            .setUpdatable();
    }

    String getName() {
        return name;
    }

    private JoinColumn setName() {
        String defaultName = String.format("%s_id", StringUtils.toSnakeCase(relationship.getField().getName()));

        if (joinColumn == null) name = defaultName;
        else {
            String joinColumnName = joinColumn.name();

            name = StringUtils.isBlank(joinColumnName) ? defaultName : joinColumnName;
        }

        return this;
    }

    Column getReferencedColumn() {
        return referencedColumn;
    }

    private JoinColumn setReferencedColumn() {
        Entity targetEntity = relationship.getTargetEntity();
        Column defaultReferencedColumn = targetEntity.getPrimaryKey().get(0);

        if (joinColumn == null) referencedColumn = defaultReferencedColumn;
        else {
            String referencedColumnName = joinColumn.referencedColumnName();

            try {
                referencedColumn = StringUtils.isBlank(referencedColumnName) ? defaultReferencedColumn : targetEntity.getColumn(referencedColumnName);
            } catch (NoSuchColumnException e) {
                throw new MappingException(e);
            }
        }

        return this;
    }

    boolean isNullable() {
        return nullable;
    }

    private JoinColumn setNullable() {
        if (joinColumn == null) return this;

        nullable = joinColumn.nullable();
        return this;
    }

    boolean isUnique() {
        return unique;
    }

    private JoinColumn setUnique() {
        if (joinColumn == null) return this;

        unique = joinColumn.unique();
        return this;
    }

    boolean isUpdatable() {
        return updatable;
    }

    private JoinColumn setUpdatable() {
        if (joinColumn == null) return this;

        updatable = joinColumn.updatable();
        return this;
    }
}
