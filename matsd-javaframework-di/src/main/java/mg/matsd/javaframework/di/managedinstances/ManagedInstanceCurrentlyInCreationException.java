package mg.matsd.javaframework.di.managedinstances;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ManagedInstanceCurrentlyInCreationException extends BaseException {
    public ManagedInstanceCurrentlyInCreationException(String managedInstanceId) {
        super(String.format(
            "La \"ManagedInstance\" avec l'identifiant \"%s\" est actuellement en cours de création, " +
            "ce qui entraîne un problème de dépendance circulaire", managedInstanceId
        ));
    }
}
