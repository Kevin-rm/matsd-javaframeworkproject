package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.utils.StringUtils;
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
    private static final Map<Class<? extends Annotation>, RelationshipType> ANNOTATION_TO_RELATION_TYPE;

    static {
        ANNOTATION_TO_RELATION_TYPE = new HashMap<>();

        ANNOTATION_TO_RELATION_TYPE.put(ManyToMany.class, RelationshipType.MANY_TO_MANY);
        ANNOTATION_TO_RELATION_TYPE.put(ManyToOne.class,  RelationshipType.MANY_TO_ONE);
        ANNOTATION_TO_RELATION_TYPE.put(OneToMany.class,  RelationshipType.ONE_TO_MANY);
        ANNOTATION_TO_RELATION_TYPE.put(OneToOne.class,   RelationshipType.ONE_TO_ONE);
    }

    private final Entity entity;
    private final Field  field;
    private RelationshipType relationshipType;
    private Class<?> targetEntityClass;
    private String  mappedBy;
    private boolean optional      = false;
    private boolean orphanRemoval = false;
    private FetchType fetchType;

    Relationship(Entity entity, Field field) {
        this.entity = entity;
        this.field  = field;
        this.setRelationshipType()
            .setTargetEntityClass()
            .setMappedBy()
            .setOptional()
            .setOrphanRemoval()
            .setFetchType();
    }

    RelationshipType getRelationshipType() {
        return relationshipType;
    }

    private Relationship setRelationshipType() {
        relationshipType = ANNOTATION_TO_RELATION_TYPE.entrySet()
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

    @SuppressWarnings("all")
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
                    field.getName(), entity.getClazz().getName(), targetEntityClass.getName(), fieldType.getName()));
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
            throw new MappingException(
                String.format("La classe d'entité cible \"%s\" est identique à l'entité actuelle \"%s\" pour le champ \"%s\". " +
                    "Les relations ne peuvent pas se référer à l'entité elle-même.",
                targetEntityClass.getName(), entity.getClazz().getName(), field.getName()));
        UtilFunctions.assertIsEntity(targetEntityClass);

        this.targetEntityClass = targetEntityClass;
        return this;
    }

    String getMappedBy() {
        return mappedBy;
    }

    @SuppressWarnings("all")
    private Relationship setMappedBy() {
        String mappedBy = null;
        if (relationshipType == RelationshipType.MANY_TO_MANY)
            mappedBy = field.getAnnotation(ManyToMany.class).mappedBy();
        else if (relationshipType == RelationshipType.ONE_TO_MANY)
            mappedBy = field.getAnnotation(OneToMany.class).mappedBy();
        else if (relationshipType == RelationshipType.ONE_TO_ONE)
            mappedBy = field.getAnnotation(OneToOne.class).mappedBy();

        if (StringUtils.isBlank(mappedBy)) return this;

        try {
            targetEntityClass.getDeclaredField(mappedBy);
        } catch (NoSuchFieldException e) {
            throw new MappingException(String.format("Le champ \"%s\" n'existe pas dans l'entité cible \"%s\"",
                mappedBy, targetEntityClass.getName()
            ));
        }

        this.mappedBy = mappedBy;
        return this;
    }

    public boolean isOptional() {
        return optional;
    }

    private Relationship setOptional() {
        if (relationshipType == RelationshipType.MANY_TO_ONE)
            optional = field.getAnnotation(ManyToOne.class).optional();
        else if (relationshipType == RelationshipType.ONE_TO_ONE)
            optional = field.getAnnotation(OneToOne.class).optional();

        return this;
    }

    public boolean isOrphanRemoval() {
        return orphanRemoval;
    }

    private Relationship setOrphanRemoval() {
        if (relationshipType == RelationshipType.ONE_TO_MANY)
            orphanRemoval = field.getAnnotation(OneToMany.class).orphanRemoval();
        else if (relationshipType == RelationshipType.ONE_TO_ONE)
            orphanRemoval = field.getAnnotation(OneToOne.class).orphanRemoval();

        return this;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    private Relationship setFetchType() {
        switch (relationshipType) {
            case MANY_TO_MANY -> fetchType = field.getAnnotation(ManyToMany.class).fetchType();
            case MANY_TO_ONE  -> fetchType = field.getAnnotation(ManyToOne.class).fetchType();
            case ONE_TO_MANY  -> fetchType = field.getAnnotation(OneToMany.class).fetchType();
            case ONE_TO_ONE   -> fetchType = field.getAnnotation(OneToOne.class).fetchType();
        }

        return this;
    }

    private enum RelationshipType { MANY_TO_MANY, MANY_TO_ONE, ONE_TO_MANY, ONE_TO_ONE }
}
