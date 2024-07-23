package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.managedinstances.ConstructorArgument;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.NoSuchManagedInstanceException;

import java.util.ArrayList;
import java.util.List;

class ManagedInstanceDefinitionRegistry {
    private final List<ManagedInstance>  managedInstances;
    private final ManagedInstanceFactory managedInstanceFactory;

    ManagedInstanceDefinitionRegistry(ManagedInstanceFactory managedInstanceFactory) {
        managedInstances = new ArrayList<>();
        this.managedInstanceFactory = managedInstanceFactory;
    }

    List<ManagedInstance> getManagedInstances() {
        return managedInstances;
    }

    Boolean containsManagedInstance(String id) {
        try {
            getManagedInstanceById(id);
            return true;
        } catch (NoSuchManagedInstanceException e) {
            return false;
        }
    }

    ManagedInstance getManagedInstanceById(String id) throws NoSuchManagedInstanceException {
        for (ManagedInstance managedInstance : managedInstances)
            if (managedInstance.getId().equals(id)) return managedInstance;

        throw new NoSuchManagedInstanceException(String.format("Aucune \"ManagedInstance\" trouvée avec l'identifiant : %s", id));
    }

    ManagedInstance getManagedInstanceByClass(Class<?> clazz) throws NoSuchManagedInstanceException {
        for (ManagedInstance managedInstance : managedInstances)
            if (managedInstance.getClazz() == clazz) return managedInstance;

        throw new NoSuchManagedInstanceException(String.format(
            "Aucune \"ManagedInstance\" trouvée ayant comme nom de classe : %s", clazz.getName())
        );
    }

    void registerManagedInstance(ManagedInstance managedInstance) {
        for (ManagedInstance m : this.managedInstances)
            if (managedInstance.getId().equals(m.getId()))
                throw new ManagedInstanceDefinitionException(
                    String.format("L'indetifiant d'une \"ManagedInstance\" doit être unique, \"%s\" est redondant", managedInstance.getId())
                );

        managedInstances.add(managedInstance);
    }

    void registerManagedInstance(String id, String clazz, String scope) {
        registerManagedInstance(new ManagedInstance(id, clazz, scope));
    }

    void configureDependencies() {
        for (ManagedInstance managedInstance : managedInstances) {
            managedInstance.getProperties().stream()
                .filter(property -> property.getRef() != null)
                .forEachOrdered(property -> {
                    String ref = property.getRef();
                    if (!containsManagedInstance(ref))
                        throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                            String.format("Aucune \"ManagedInstance\" trouvée avec la référence : \"%s\"", ref)
                        ));

                    property.setValue(managedInstanceFactory.getManagedInstance(ref));
                });

            for (ConstructorArgument constructorArgument : managedInstance.getConstructorArguments()) {
                String ref = constructorArgument.getRef();

                Object value;
                if (ref == null)
                    try {
                        value = managedInstanceFactory.getManagedInstance(constructorArgument.getType());
                    } catch (NoSuchManagedInstanceException e) {
                        throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                            String.format(
                                "Erreur de résolution de dépendance pour la \"ManagedInstance\" avec l'identifiant \"%s\" car " +
                                "aucune \"ManagedInstance\" n'a été trouvée pour le type \"%s\"",
                                managedInstance.getId(), constructorArgument.getType())
                        ));
                    }
                else
                    try {
                        value = managedInstanceFactory.getManagedInstance(ref);
                    } catch (NoSuchManagedInstanceException e) {
                        throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                            String.format("Aucune \"ManagedInstance\" trouvée avec la référence : \"%s\"", ref)
                        ));
                    }

                constructorArgument.setValue(value);
            }
        }
    }
}
