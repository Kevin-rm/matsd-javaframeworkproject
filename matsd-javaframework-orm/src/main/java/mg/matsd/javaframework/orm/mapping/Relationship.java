package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.annotations.ManyToMany;
import mg.matsd.javaframework.orm.annotations.ManyToOne;
import mg.matsd.javaframework.orm.annotations.OneToMany;
import mg.matsd.javaframework.orm.annotations.OneToOne;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static mg.matsd.javaframework.orm.mapping.RelationshipType.*;

public class Relationship {
    private static final Map<RelationshipType, Class<? extends Annotation>> RELATION_TYPE_ANNOTATION_MAP = Map.of(
        MANY_TO_MANY, ManyToMany.class,
        MANY_TO_ONE,  ManyToOne.class,
        ONE_TO_MANY,  OneToMany.class,
        ONE_TO_ONE,   OneToOne.class
    );

    private final Entity entity;
    private final Field  field;
    private RelationshipType relationshipType;
    private Entity targetEntity;
    @Nullable
    private Field  mappedBy;
    @Nullable
    private List<JoinColumn> joinColumns;
    @Nullable
    private JoinTable joinTable;
    private boolean optional      = false;
    private boolean orphanRemoval = false;
    private FetchType fetchType;

    Relationship(Entity entity, Field field) {
        this.entity = entity;
        this.field  = field;
        this.setRelationshipType()
            .setTargetEntity()
            .setMappedBy()
            .setJoinColumns()
            .setJoinTable()
            .setOptional()
            .setOrphanRemoval()
            .setFetchType();
    }

    Entity getEntity() {
        return entity;
    }

    public Field getField() {
        return field;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    private Relationship setRelationshipType() {
        relationshipType = RELATION_TYPE_ANNOTATION_MAP
            .entrySet()
            .stream()
            .filter(entry -> field.isAnnotationPresent(entry.getValue()))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(relationshipType);

        return this;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    @SuppressWarnings("all")
    private Relationship setTargetEntity() {
        Class<?> targetEntityClass = null;
        Class<?> fieldType = field.getType();
        final String fieldName       = field.getName();
        final String entityClassName = entity.getClazz().getName();

        if (relationshipType == MANY_TO_ONE || relationshipType == ONE_TO_ONE) {
            if (field.isAnnotationPresent(ManyToOne.class))
                targetEntityClass = field.getAnnotation(ManyToOne.class).targetEntity();
            else if (field.isAnnotationPresent(OneToOne.class))
                targetEntityClass = field.getAnnotation(OneToOne.class).targetEntity();

            if (targetEntityClass == void.class) targetEntityClass = fieldType;
            else if (!fieldType.isAssignableFrom(targetEntityClass))
                throw new MappingException(
                    String.format("La classe d'entité cible définie dans l'annotation pour le champ \"%s\" de l'entité \"%s\" " +
                        "est \"%s\", mais le type réel du champ est \"%s\"",
                    fieldName, entityClassName, targetEntityClass.getName(), fieldType.getName()));
        } else {
            if (!Collection.class.isAssignableFrom(fieldType))
                throw new MappingException(String.format("Les colonnes de type relation (%s et %s) " +
                    "doivent être des collections, mais le champ \"%s\" de l'entité \"%s\" est de type \"%s\"",
                    MANY_TO_MANY, ONE_TO_MANY, fieldName, entityClassName, fieldType)
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
                            fieldName, entityClassName)
                    );

                targetEntityClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
            }
        }

        Entity targetEntity = entity.getSessionFactoryOptions().getEntity(targetEntityClass);
        if (targetEntity == null)
            throw new MappingException(String.format("La classe d'entité cible \"%s\" du champ \"%s\" de l'entité \"%s\" n'est pas une entité",
                targetEntityClass.getName(), fieldName, entityClassName
            ));

        if (targetEntityClass == entity.getClazz())
            throw new MappingException(
                String.format("La classe d'entité cible \"%s\" est identique à l'entité actuelle \"%s\" pour le champ \"%s\". " +
                    "Les relations ne peuvent pas se référer à l'entité elle-même",
                targetEntityClass.getName(), entityClassName, fieldName));

        this.targetEntity = targetEntity;
        return this;
    }

    @Nullable
    public Field getMappedBy() {
        return mappedBy;
    }

    @SuppressWarnings("all")
    private Relationship setMappedBy() {
        String mappedBy = null;
        if (relationshipType == MANY_TO_MANY)
            mappedBy = field.getAnnotation(ManyToMany.class).mappedBy();
        else if (relationshipType == ONE_TO_MANY)
            mappedBy = field.getAnnotation(OneToMany.class).mappedBy();
        else if (relationshipType == ONE_TO_ONE)
            mappedBy = field.getAnnotation(OneToOne.class).mappedBy();

        if (mappedBy == null || StringUtils.isBlank(mappedBy)) return this;

        Class<?> targetEntityClass = targetEntity.getClazz();
        try {
            mappedBy = mappedBy.strip();
            Field f = targetEntityClass.getDeclaredField(mappedBy);

            Class<? extends Annotation> expectedAnnotation = null;
            for (Map.Entry<RelationshipType, Class<? extends Annotation>> entry : RELATION_TYPE_ANNOTATION_MAP.entrySet())
                if (relationshipType == entry.getKey()) {
                    expectedAnnotation = entry.getValue();
                    break;
                }

            if (!f.isAnnotationPresent(expectedAnnotation))
                throw new MappingException(String.format(
                    "Le champ \"%s\" de l'entité cible \"%s\" n'a pas l'annotation attendue \"%s\" qui inverse la relation",
                    mappedBy, targetEntityClass.getName(), expectedAnnotation.getSimpleName()
                ));

            this.mappedBy = f;
        } catch (NoSuchFieldException e) {
            throw new MappingException(String.format(
                "Le champ \"%s\" spécifié dans \"mappedBy\" sur l'annotation du champ \"%s\" de l'entité actuelle \"%s\", n'existe pas dans l'entité cible \"%s\"",
                mappedBy, field.getName(), entity.getClazz().getName(), targetEntityClass.getName()
            ));
        }

        return this;
    }

    @Nullable
    public List<JoinColumn> getJoinColumns() {
        return joinColumns;
    }

    private Relationship setJoinColumns() {
        if (relationshipType  != MANY_TO_ONE &&
            (relationshipType != ONE_TO_ONE || mappedBy != null)
        ) return this;

        mg.matsd.javaframework.orm.annotations.JoinColumn[] joinColumns = null;
        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.JoinColumn.class))
            joinColumns = field.getAnnotationsByType(mg.matsd.javaframework.orm.annotations.JoinColumn.class);

        this.joinColumns = new ArrayList<>();
        if (joinColumns == null || joinColumns.length == 0)
            this.joinColumns.add(new JoinColumn(this, null));
        else Arrays.stream(joinColumns)
            .forEachOrdered(joinColumn ->
                this.joinColumns.add(new JoinColumn(this, joinColumn))
            );

        return this;
    }

    @Nullable
    public JoinTable getJoinTable() {
        return joinTable;
    }

    private Relationship setJoinTable() {
        if ((relationshipType != MANY_TO_MANY && relationshipType != ONE_TO_MANY) || mappedBy != null)
            return this;

        mg.matsd.javaframework.orm.annotations.JoinTable j = null;
        if (field.isAnnotationPresent(mg.matsd.javaframework.orm.annotations.JoinTable.class))
            j = field.getAnnotation(mg.matsd.javaframework.orm.annotations.JoinTable.class);

        joinTable = new JoinTable(this, j);
        return this;
    }

    public boolean isOptional() {
        return optional;
    }

    private Relationship setOptional() {
        if (relationshipType == MANY_TO_ONE)
            optional = field.getAnnotation(ManyToOne.class).optional();
        else if (relationshipType == ONE_TO_ONE)
            optional = field.getAnnotation(OneToOne.class).optional();

        return this;
    }

    public boolean isOrphanRemoval() {
        return orphanRemoval;
    }

    private Relationship setOrphanRemoval() {
        if (relationshipType == ONE_TO_MANY)
            orphanRemoval = field.getAnnotation(OneToMany.class).orphanRemoval();
        else if (relationshipType == ONE_TO_ONE)
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

    public boolean isToOne() {
        return relationshipType == MANY_TO_ONE || relationshipType == ONE_TO_ONE;
    }

    public boolean isToMany() {
        return !isToOne();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relationship that = (Relationship) o;

        if (!Objects.equals(entity, that.entity)) return false;
        if (!Objects.equals(field, that.field)) return false;
        if (relationshipType != that.relationshipType) return false;
        if (!Objects.equals(targetEntity, that.targetEntity)) return false;
        return fetchType == that.fetchType;
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        result = 31 * result + (field != null ? field.hashCode() : 0);
        result = 31 * result + (relationshipType != null ? relationshipType.hashCode() : 0);
        result = 31 * result + (targetEntity != null ? targetEntity.hashCode() : 0);
        result = 31 * result + (fetchType != null ? fetchType.hashCode() : 0);
        return result;
    }
}
