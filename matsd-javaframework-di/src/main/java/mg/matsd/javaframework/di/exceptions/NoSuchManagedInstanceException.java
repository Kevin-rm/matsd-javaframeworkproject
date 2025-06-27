package mg.matsd.javaframework.di.exceptions;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.exceptions.BaseException;

public class NoSuchManagedInstanceException extends BaseException {
    @Nullable
    private String managedInstanceId;
    @Nullable
    private Class<?> managedInstanceClass;

    public NoSuchManagedInstanceException(String message) {
        super(message);
    }

    public NoSuchManagedInstanceException(String managedInstanceId, boolean asReference) {
        super(String.format("Aucune \"ManagedInstance\" trouvée avec %s : %s",
            asReference ? "la référence" : "l'identifiant", managedInstanceId));
        this.managedInstanceId = managedInstanceId;
    }

    public NoSuchManagedInstanceException(Class<?> managedInstanceClass) {
        super(String.format("Aucune \"ManagedInstance\" trouvée ayant " +
            "comme nom de classe : %s", managedInstanceClass.getName()
        ));
        this.managedInstanceClass = managedInstanceClass;
    }

    @Nullable
    public String getManagedInstanceId() {
        return managedInstanceId;
    }

    @Nullable
    public Class<?> getManagedInstanceClass() {
        return managedInstanceClass;
    }
}
