package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.exceptions.BaseException;

class ManagedInstanceCurrentlyInCreationException extends BaseException {
    ManagedInstanceCurrentlyInCreationException(ManagedInstance managedInstance, Class<?> type) {
        super(String.format(
            "Erreur lors de la création de la \"ManagedInstance\" avec l'identifiant \"%s\". " +
            "Une dépendance de type \"%s\" est en cours de création, " +
            "ce qui entraîne un problème de dépendance circulaire",
            managedInstance.getId(), type
        ));
    }
}
