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

        if (!Objects.equals(primaryKeyValue, that.primaryKeyValue))
            return false;
        return Objects.equals(relationship, that.relationship);
    }
}
