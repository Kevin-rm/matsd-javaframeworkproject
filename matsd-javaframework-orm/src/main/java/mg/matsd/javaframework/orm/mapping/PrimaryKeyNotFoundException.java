package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.exceptions.BaseException;

class PrimaryKeyNotFoundException extends BaseException {
    PrimaryKeyNotFoundException(Class<?> entityClass) {
        super(String.format("Clé primaire non précisée pour l'entité %s", entityClass.getName()));
    }
}
