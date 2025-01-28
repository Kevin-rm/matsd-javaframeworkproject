package mg.itu.prom16.base.internal;

import mg.matsd.javaframework.core.exceptions.BaseException;

import java.lang.reflect.Field;

public class NonInitializedCollectionException extends BaseException {
    private static final String PREFIX = "Collection non initialisée";
    private final String   modelName;
    private final Class<?> modelType;
    private final Field field;

    NonInitializedCollectionException(String modelName, Class<?> modelType, Field field) {
        super(String.format("La valeur du champ collection \"%s\" sur le modèle \"%s\" de type \"%s\" est \"null\"",
            field.getName(), modelName, modelType), PREFIX);

        this.modelName = modelName;
        this.modelType = modelType;
        this.field = field;
    }

    public String getModelName() {
        return modelName;
    }

    public Class<?> getModelType() {
        return modelType;
    }

    public Field getField() {
        return field;
    }
}
