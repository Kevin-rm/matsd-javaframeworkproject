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
        Class<?> targetEntityClass = null;
        Class<?> fieldType = field.getType();

        if (relationshipType == RelationshipType.MANY_TO_ONE || relationshipType == RelationshipType.ONE_TO_ONE) {
            if (field.isAnnotationPresent(ManyToOne.class))
                targetEntityClass = field.getAnnotation(ManyToOne.class).targetEntity();
            else if (field.isAnnotationPresent(OneToOne.class))
                targetEntityClass = field.getAnnotation(OneToOne.class).targetEntity();

            if (targetEntityClass == void.class) targetEntityClass = fieldType;
            else if (targetEntityClass != fieldType)
                throw new MappingException(
                    String.format("La classe d'entité cible définie dans l'annotation pour le champ \"%s\" de l'entité \"%s\" " +
                        "est \"%s\", mais le type réel du champ est \"%s\". Ils doivent correspondre.",
                    field.getName(), entity.getClazz().getName(), targetEntityClass.getName(), fieldType.getName())
                );
        } else {
            if (!Collection.class.isAssignableFrom(fieldType))
                throw new MappingException(String.format("Les colonnes de type relation (%s et %s) " +
                    "doivent être des collections, mais le champ \"%s\" de l'entité \"%s\" est de type \"%s\"",
                    RelationshipType.MANY_TO_MANY, RelationshipType.ONE_TO_MANY, field.getName(), entity.getClazz().getName(), fieldType)
                );

            if (field.isAnnotationPresent(ManyToMany.class))
                targetEntityClass = field.getAnnotation(ManyToMany.class).targetEntity();
            else if (field.isAnnotationPresent(OneToMany.class))
                targetEntityClass = field.getAnnotation(OneToMany.class).targetEntity();

            Type genericType = field.getGenericType();
            if (targetEntityClass == void.class) {
                if (!(genericType instanceof ParameterizedType))
                    throw new MappingException(
                        String.format("Le champ \"%s\" de l'entité \"%s\" est une collection mais " +
                                "le type générique de la collection n'est pas spécifié, et aucune entité cible n'est définie dans l'annotation",
                            field.getName(), entity.getClazz().getName())
                    );

                targetEntityClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
            }
        }

        if (targetEntityClass == entity.getClazz())
            throw new MappingException(String.format("La classe d'entité cible \"%s\" est identique à l'entité actuelle \"%s\" pour le champ \"%s\". " +
                    "Les relations ne peuvent pas se référer à l'entité elle-même.",
                targetEntityClass.getName(), entity.getClazz().getName(), field.getName()));
        UtilFunctions.assertIsEntity(targetEntityClass);

        this.targetEntityClass = targetEntityClass;
        return this;
    }

    String getMappedBy() {
        return mappedBy;
    }

    private Relationship setMappedBy() {
        

        this.mappedBy = mappedBy;
        return this;
    }

    private enum RelationshipType { MANY_TO_MANY, MANY_TO_ONE, ONE_TO_MANY, ONE_TO_ONE }
}
