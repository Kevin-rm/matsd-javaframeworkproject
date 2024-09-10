package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.orm.annotations.ManyToMany;
import mg.matsd.javaframework.orm.annotations.ManyToOne;
import mg.matsd.javaframework.orm.annotations.OneToMany;
import mg.matsd.javaframework.orm.annotations.OneToOne;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
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
    private Class<?> targetEntityClass;
    private String  mappedBy;
    private boolean optional      = false;
    private boolean orphanRemoval = false;
    private FetchType fetchType   = FetchType.LAZY;

    Relationship(Entity entity, Field field) {
        this.entity = entity;
        this.field  = field;

        this.setRelationshipType()
            .setTargetEntityClass();
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

    Class<?> getTargetEntityClass() {
        return targetEntityClass;
    }

    private Relationship setTargetEntityClass() {
        Class<?> fieldType = field.getType();
        if (relationshipType == RelationshipType.MANY_TO_ONE || relationshipType == RelationshipType.ONE_TO_ONE) {
            UtilFunctions.assertIsEntity(fieldType);

            targetEntityClass = fieldType;
        } else {
            if (!Collection.class.isAssignableFrom(fieldType))
                throw new MappingException(String.format("Les colonnes de type relation (%s et %s) " +
                    "doivent être des collections, mais le champ \"%s\" de l'entité \"%s\" est de type \"%s\"",
                    RelationshipType.MANY_TO_MANY, RelationshipType.ONE_TO_MANY, field.getName(), entity.getClazz().getName(), fieldType)
                );

            Class<?> c = null;
            if (field.isAnnotationPresent(ManyToMany.class))
                c = field.getAnnotation(ManyToMany.class).targetEntity();
            else if (field.isAnnotationPresent(ManyToOne.class))
                c = field.getAnnotation(ManyToOne.class).targetEntity();

            Type genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType) && c == void.class)
                throw new MappingException(
                    String.format("Le champ \"%s\" de l'entité \"%s\" est une collection mais " +
                        "le type générique de la collection n'est pas spécifié, et aucune entité cible n'est définie dans l'annotation",
                    field.getName(), entity.getClazz().getName())
                );


            if (c == void.class) {
                c = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
                UtilFunctions.assertIsEntity(c);
            }
            targetEntityClass = c;
        }

        if (targetEntityClass == entity.getClazz())
            throw new MappingException(String.format("La classe d'entité cible \"%s\" est identique à l'entité actuelle \"%s\" pour le champ \"%s\". " +
                    "Les relations ne peuvent pas se référer à l'entité elle-même.",
                targetEntityClass.getName(), entity.getClazz().getName(), field.getName()));

        return this;
    }

    String getMappedBy() {
        return mappedBy;
    }

    public Relationship setMappedBy() {
        

        this.mappedBy = mappedBy;
        return this;
    }

    private enum RelationshipType { MANY_TO_MANY, MANY_TO_ONE, ONE_TO_MANY, ONE_TO_ONE }
}
