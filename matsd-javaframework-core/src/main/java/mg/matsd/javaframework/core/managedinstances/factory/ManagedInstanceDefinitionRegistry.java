package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.annotations.Component;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.annotations.Scope;
import mg.matsd.javaframework.core.exceptions.PackageNotFoundException;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.NoSuchManagedInstanceException;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ManagedInstanceDefinitionRegistry {
    private final List<ManagedInstance>  managedInstances;
    private final ManagedInstanceFactory managedInstanceFactory;

    ManagedInstanceDefinitionRegistry(ManagedInstanceFactory managedInstanceFactory) {
        managedInstances = new ArrayList<>();
        this.managedInstanceFactory = managedInstanceFactory;
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

    void doScanComponents(String packageName) {
        URL url = Thread.currentThread()
            .getContextClassLoader()
            .getResource(packageName.replace('.', '/'));

        if (url == null) throw new PackageNotFoundException(packageName);

        File[] files = new File(url.getPath()).listFiles();
        if (files == null) return;

        for (File file : files) {
            String fileName = file.getName();

            if (file.isDirectory()) doScanComponents(String.format("%s.%s", packageName, fileName));

            try {
                Class<?> clazz = Class.forName(
                    String.format("%s.%s", packageName, fileName.replaceAll("\\.class$", ""))
                );

                if (!isComponent(clazz)) continue;

                Component component = (Component) AnnotationUtils.getAnnotation(Component.class, clazz);
                String scope = null;
                if (AnnotationUtils.hasAnnotation(Scope.class, clazz))
                    scope = ((Scope) AnnotationUtils.getAnnotation(Scope.class, clazz)).value();

                registerManagedInstance(new ManagedInstance(
                    StringUtils.isBlank(component.value()) ? null : component.value(),
                    clazz, scope
                ));
            } catch (ClassNotFoundException ignored) { }
        }
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
        managedInstances.stream()
            .flatMap(managedInstance -> managedInstance.getProperties().stream())
            .filter(property -> property.getRef() != null)
            .forEach(property -> {
                String ref = property.getRef();

                if (!containsManagedInstance(ref))
                    throw new ManagedInstanceDefinitionException(
                        new NoSuchManagedInstanceException(
                            String.format("Aucune \"ManagedInstance\" trouvée avec la référence : \"%s\"", ref)
                        )
                    );

                property.setValue(managedInstanceFactory.getManagedInstance(ref));
            });
    }

    private boolean isComponent(@Nullable Class<?> clazz) {
        if (clazz == null) return false;

        return AnnotationUtils.hasAnnotation(Component.class, clazz);
    }
}
