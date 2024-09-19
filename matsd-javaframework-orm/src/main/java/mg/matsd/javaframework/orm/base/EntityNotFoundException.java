package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.core.exceptions.BaseException;

class EntityNotFoundException extends BaseException {
    EntityNotFoundException(Class<?> clazz) {
        super(String.format("Aucune entité trouvée ayant comme nom de classe : \"%s\"", clazz.getName()));
    }
}
