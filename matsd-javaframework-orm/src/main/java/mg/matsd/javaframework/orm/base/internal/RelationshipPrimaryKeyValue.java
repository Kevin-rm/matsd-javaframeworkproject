package mg.matsd.javaframework.orm.base.internal;

import mg.matsd.javaframework.orm.mapping.Relationship;

import java.util.List;
import java.util.Objects;

public record RelationshipPrimaryKeyValue(Relationship relationship, List<Object> primaryKeyValue) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelationshipPrimaryKeyValue that = (RelationshipPrimaryKeyValue) o;

        if (!Objects.equals(relationship, that.relationship)) return false;
        return Objects.equals(primaryKeyValue, that.primaryKeyValue);
    }

    @Override
    public int hashCode() {
        int result = relationship != null ? relationship.hashCode() : 0;
        result = 31 * result + (primaryKeyValue != null ? primaryKeyValue.hashCode() : 0);
        return result;
    }
}
