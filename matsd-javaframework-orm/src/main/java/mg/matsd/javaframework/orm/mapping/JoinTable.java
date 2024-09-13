package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;

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
        String name = joinTable.name();

        this.name = StringUtils.isBlank(name) ?
            String.format("%s_%s_association", relationship.getEntity().getTableName(), relationship.getTargetEntity().getTableName()) : name;
        return this;
    }

    List<JoinColumn> getJoinColumns() {
        return joinColumns;
    }

    private JoinTable setJoinColumns() {


        return this;
    }

    List<JoinColumn> getInverseJoinColumns() {
        return inverseJoinColumns;
    }

    private JoinTable setInverseJoinColumns() {

        return this;
    }
}
