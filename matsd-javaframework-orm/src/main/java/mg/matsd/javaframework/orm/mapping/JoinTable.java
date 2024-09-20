package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

class JoinTable {
    private final Relationship relationship;
    @Nullable
    private final mg.matsd.javaframework.orm.annotations.JoinTable joinTable;
    private String name;
    private List<JoinColumn> joinColumns;
    private List<JoinColumn> inverseJoinColumns;

    JoinTable(Relationship relationship, @Nullable mg.matsd.javaframework.orm.annotations.JoinTable joinTable) {
        this.relationship = relationship;
        this.joinTable    = joinTable;
        this.setName()
            .setJoinColumns()
            .setInverseJoinColumns();
    }

    String getName() {
        return name;
    }

    private JoinTable setName() {
        String defaultName = String.format("%s_%s_association", relationship.getEntity().getTableName(), relationship.getTargetEntity().getTableName());

        if (joinTable == null) name = defaultName;
        else {
            String joinTableName = joinTable.name();

            name = StringUtils.isBlank(joinTableName) ? defaultName : joinTableName;
        }

        return this;
    }

    List<JoinColumn> getJoinColumns() {
        return joinColumns;
    }

    private JoinTable setJoinColumns() {
        List<Column> primaryKeyColumns = relationship.getEntity().getPrimaryKey();

        joinColumns = setJoinColumns(primaryKeyColumns, joinTable == null ? null : joinTable.joinColumns());
        return this;
    }

    List<JoinColumn> getInverseJoinColumns() {
        return inverseJoinColumns;
    }

    private JoinTable setInverseJoinColumns() {
        List<Column> primaryKeyColumns = relationship.getTargetEntity().getPrimaryKey();

        inverseJoinColumns = setJoinColumns(primaryKeyColumns, joinTable == null ? null : joinTable.inverseJoinColumns());
        return this;
    }

    private List<JoinColumn> setJoinColumns(List<Column> primaryKeyColumns, @Nullable mg.matsd.javaframework.orm.annotations.JoinColumn[] joinColumnsAnnotations) {
        List<JoinColumn> joinColumns = new ArrayList<>();

        if (joinColumnsAnnotations == null || joinColumnsAnnotations.length == 0)
            primaryKeyColumns.forEach(primaryKeyColumn ->
                joinColumns.add(new JoinColumn(relationship, null))
            );
        else for (mg.matsd.javaframework.orm.annotations.JoinColumn joinColumnAnnotation : joinColumnsAnnotations)
                joinColumns.add(new JoinColumn(relationship, joinColumnAnnotation));

        return joinColumns;
    }
}
