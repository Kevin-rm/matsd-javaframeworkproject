package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.exceptions.BaseException;

class NoSuchColumnException extends BaseException {
    NoSuchColumnException(Entity entity, String notFoundColumn) {
        super(String.format("Aucune colonne identifiée par le nom de champ \"%s\" dans l'entité \"%s\"",
            notFoundColumn, entity.getClazz().getName()
        ));
    }
}
