package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.exceptions.BaseException;

class MissingPrimaryKeyException extends BaseException {
    MissingPrimaryKeyException(Entity entity) {
        super(String.format("Clé primaire non précisée pour l'entité %s", entity.getClazz().getName()));
    }
}
