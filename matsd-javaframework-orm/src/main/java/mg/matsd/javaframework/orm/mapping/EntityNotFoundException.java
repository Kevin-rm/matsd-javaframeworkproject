package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class EntityNotFoundException extends BaseException {
    public EntityNotFoundException(Class<?> clazz) {
        super(String.format("Aucune entité trouvée ayant comme nom de classe : \"%s\"", clazz.getName()));
    }
}
