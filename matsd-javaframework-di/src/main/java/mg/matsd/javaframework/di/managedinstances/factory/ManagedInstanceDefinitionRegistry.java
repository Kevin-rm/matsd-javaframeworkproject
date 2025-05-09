package mg.matsd.javaframework.di.managedinstances.factory;

import mg.matsd.javaframework.di.exceptions.NoSuchManagedInstanceException;
import mg.matsd.javaframework.di.managedinstances.ManagedInstance;
import mg.matsd.javaframework.di.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.di.managedinstances.Scope;

import java.lang.reflect.Method;
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

    boolean containsManagedInstance(String id) {
        try {
            getManagedInstanceById(id);
            return true;
        } catch (NoSuchManagedInstanceException e) {
            return false;
        }
    }

    boolean containsManagedInstance(Class<?> clazz) {
        try {
            getManagedInstanceByClass(clazz);
            return true;
        } catch (NoSuchManagedInstanceException e) {
            return false;
        }
    }

    ManagedInstance getManagedInstanceById(String id) throws NoSuchManagedInstanceException {
        for (ManagedInstance managedInstance : managedInstances)
            if (managedInstance.getId().equals(id)) return managedInstance;

        throw new NoSuchManagedInstanceException(id, false);
    }

    ManagedInstance getManagedInstanceByClass(Class<?> clazz) throws NoSuchManagedInstanceException {
        for (ManagedInstance managedInstance : managedInstances)
            if (clazz.isAssignableFrom(managedInstance.getClazz())) return managedInstance;

        throw new NoSuchManagedInstanceException(clazz);
    }

    void registerManagedInstance(ManagedInstance managedInstance) {
        managedInstances.stream()
            .filter(m -> managedInstance.getId().equals(m.getId()))
            .forEachOrdered(m -> {
                throw new ManagedInstanceDefinitionException(String.format("L'identifiant d'une \"ManagedInstance\" doit être unique, " +
                    "\"%s\" est redondant", managedInstance.getId()));
            });

        managedInstances.add(managedInstance);
    }

    void registerManagedInstance(
        String id, Class<?> clazz, Scope scope, Boolean isLazy, String parentId, String factoryMethodName
    ) {
        ManagedInstance parent = getManagedInstanceById(parentId);
        Class<?> parentClass   = parent.getClazz();
        try {
            Method method = parentClass.getMethod(factoryMethodName);
            ManagedInstance managedInstance = new ManagedInstance(id, clazz, scope, isLazy, parent, method);
            ManagedInstanceUtils.addConstructorArguments(method, managedInstance);

            registerManagedInstance(managedInstance);
        } catch (NoSuchMethodException e) {
            throw new ManagedInstanceDefinitionException(String.format("Aucune méthode nommée \"%s\" " +
                "sur la classe parente \"%s\"", factoryMethodName, parentClass.getName()));
        }
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
                        reference, true
                    ));

                property.setValue(managedInstanceFactory.getManagedInstance(reference));
            });
    }

    private void resolveConstructorArgumentDependencies(ManagedInstance managedInstance) {
        String managedInstanceId = managedInstance.getId();
        try {
            managedInstancesCurrentlyInCreation.add(managedInstanceId);

            managedInstance.getConstructorArguments().stream()
                .filter(constructorArgument -> constructorArgument.getValue() == null)
                .forEachOrdered(constructorArgument -> {
                    String reference = constructorArgument.getReference();
                    Class<?> constructorArgumentType = constructorArgument.getType();

                    Object value;
                    if (reference == null) try {
                        value = managedInstanceFactory.getManagedInstance(constructorArgumentType);
                    } catch (NoSuchManagedInstanceException e) {
                        throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                            String.format(
                                "Erreur de résolution de dépendance pour la \"ManagedInstance\" avec l'identifiant \"%s\" car " +
                                    "aucune \"ManagedInstance\" n'a été trouvée pour le type \"%s\"",
                                managedInstanceId, constructorArgumentType)
                        ));
                    } else try {
                        value = managedInstanceFactory.getManagedInstance(reference);
                    } catch (NoSuchManagedInstanceException e) {
                        throw new ManagedInstanceDefinitionException(new NoSuchManagedInstanceException(
                            reference, true
                        ));
                    }

                    constructorArgument.setValue(value);
                });
        } finally {
            managedInstancesCurrentlyInCreation.remove(managedInstanceId);
        }
    }
}
