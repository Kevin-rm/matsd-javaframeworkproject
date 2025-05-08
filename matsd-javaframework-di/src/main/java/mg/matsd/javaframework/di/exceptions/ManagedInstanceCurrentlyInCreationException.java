package mg.matsd.javaframework.di.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

public class ManagedInstanceCurrentlyInCreationException extends BaseException {
    private final String managedInstanceId;

    public ManagedInstanceCurrentlyInCreationException(final String managedInstanceId) {
        super(String.format(
            "La \"ManagedInstance\" avec l'identifiant \"%s\" est actuellement en cours de création, " +
            "ce qui entraîne un problème de dépendance circulaire", managedInstanceId
        ));
        this.managedInstanceId = managedInstanceId;
    }

    public String getManagedInstanceId() {
        return managedInstanceId;
    }
}
