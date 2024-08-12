package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.managedinstances.ConstructorArgument;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.NoSuchManagedInstanceException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManagedInstanceDefinitionRegistry {
    private final List<ManagedInstance>  managedInstances;
    private final ManagedInstanceFactory managedInstanceFactory;
    private final Set<String> managedInstancesCurrentlyInCreation;

    ManagedInstanceDefinitionRegistry(ManagedInstanceFactory managedInstanceFactory) {
        managedInstances = new ArrayList<>();
        this.managedInstanceFactory = managedInstanceFactory;
        managedInstancesCurrentlyInCreation = new HashSet<>();
    }

    public List<ManagedInstance> getManagedInstances() {
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

    boolean isCurrentlyInCreation(String id) {
        return managedInstancesCurrentlyInCreation.contains(id);
    }

    void resolveDependencies(ManagedInstance managedInstance) {
        resolveConstructorArgumentDependencies(managedInstance);
        resolvePropertyDependencies(managedInstance);
    }

    private void resolvePropertyDependencies(ManagedInstance managedInstance) {
        managedInstance.getProperties().stream()
            .filter(property -> property.getReference() != null)
            .forEachOrdered(property -> {
                String reference = property.getReference();
                if (!containsManagedInstance(reference))
                    throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                        String.format("Aucune \"ManagedInstance\" trouvée avec la référence : \"%s\"", reference)
                    ));

                property.setValue(managedInstanceFactory.getManagedInstance(reference));
            });
    }

    private void resolveConstructorArgumentDependencies(ManagedInstance managedInstance) {
        String managedInstanceId = managedInstance.getId();
        try {
            managedInstancesCurrentlyInCreation.add(managedInstanceId);

            for (ConstructorArgument constructorArgument : managedInstance.getConstructorArguments()) {
                if (constructorArgument.getValue() != null) continue;

                String reference = constructorArgument.getReference();
                Class<?> constructorArgumentType = constructorArgument.getType();

                Object value;
                if (reference == null)
                    try {
                        value = managedInstanceFactory.getManagedInstance(constructorArgumentType);
                    } catch (NoSuchManagedInstanceException e) {
                        throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                            String.format(
                                "Erreur de résolution de dépendance pour la \"ManagedInstance\" avec l'identifiant \"%s\" car " +
                                    "aucune \"ManagedInstance\" n'a été trouvée pour le type \"%s\"",
                                managedInstanceId, constructorArgumentType)
                        ));
                    }
                else
                    try {
                        value = managedInstanceFactory.getManagedInstance(reference);
                    } catch (NoSuchManagedInstanceException e) {
                        throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                            String.format("Aucune \"ManagedInstance\" trouvée avec la référence : \"%s\"", reference)
                        ));
                    }

                constructorArgument.setValue(value);
            }
        } finally {
            managedInstancesCurrentlyInCreation.remove(managedInstanceId);
        }
    }
}
