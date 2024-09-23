package mg.matsd.javaframework.orm.base.internal;

import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.orm.mapping.Entity;

import java.lang.reflect.Field;

class CollectionNotInitializedException extends BaseException {
    private static final String PREFIX = "Collection non initialisée";

    CollectionNotInitializedException(Entity entity, Field relationshipField) {
        super(String.format("La relation de type \"to many\" sur le champ \"%s\" de l'entité \"%s\" " +
            "n'a pas été initialisée explicitement", relationshipField.getName(), entity.getClazz()), PREFIX);
    }
}
