package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.orm.annotations.ManyToMany;
import mg.matsd.javaframework.orm.annotations.ManyToOne;
import mg.matsd.javaframework.orm.annotations.OneToMany;
import mg.matsd.javaframework.orm.annotations.OneToOne;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

class Relationship {
    private static final Map<Class<? extends Annotation>, RelationshipType> annotationToTypeMap;

    static {
        annotationToTypeMap = new HashMap<>();

        annotationToTypeMap.put(ManyToMany.class, RelationshipType.MANY_TO_MANY);
        annotationToTypeMap.put(ManyToOne.class, RelationshipType.MANY_TO_ONE);
        annotationToTypeMap.put(OneToMany.class, RelationshipType.ONE_TO_MANY);
        annotationToTypeMap.put(OneToOne.class, RelationshipType.ONE_TO_ONE);
    }

    private final Entity entity;
    private final Field  field;
    private RelationshipType relationshipType;
    private Entity targetEntity;
    private String mappedBy;
    private boolean optional      = false;
    private boolean orphanRemoval = false;
    private FetchType fetchType   = FetchType.LAZY;

    Relationship(Entity entity, Field field) {
        this.entity = entity;
        this.field  = field;

        this.setRelationshipType();
    }

    RelationshipType getRelationshipType() {
        return relationshipType;
    }

    private Relationship setRelationshipType() {
        relationshipType = annotationToTypeMap.entrySet()
            .stream()
            .filter(entry -> field.isAnnotationPresent(entry.getKey()))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(relationshipType);

        return this;
    }

    

    enum RelationshipType { MANY_TO_MANY, MANY_TO_ONE, ONE_TO_MANY, ONE_TO_ONE }
}
