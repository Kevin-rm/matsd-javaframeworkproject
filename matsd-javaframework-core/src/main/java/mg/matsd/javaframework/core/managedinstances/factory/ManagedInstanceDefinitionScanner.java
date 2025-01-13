package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.annotations.*;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.ClassScanner;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.lang.reflect.Method;

class ManagedInstanceDefinitionScanner {
    private ManagedInstanceDefinitionScanner() { }

    static void doScanComponents(ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry, String packageName) {
        ClassScanner.doScan(packageName, clazz -> {
            if (!isComponent(clazz)) return;

            Component component = (Component) AnnotationUtils.getAnnotation(Component.class, clazz);
            String scope = null;
            if (clazz.isAnnotationPresent(Scope.class))
                scope = clazz.getAnnotation(Scope.class).value();

            String componentValue = component.value();
            ManagedInstance managedInstance = new ManagedInstance(
                StringUtils.isBlank(componentValue) ? null : componentValue,
                clazz, scope, null, null
            );
            ManagedInstanceUtils.processConstructorArguments(
                ManagedInstanceUtils.constructorToUse(managedInstance), managedInstance);

            managedInstanceDefinitionRegistry.registerManagedInstance(managedInstance);

            if (clazz.isAnnotationPresent(Configuration.class))
                loadManagedInstancesFromConfiguration(managedInstanceDefinitionRegistry, managedInstance);
        });
    }

    private static void loadManagedInstancesFromConfiguration(
        ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry, ManagedInstance configuration
    ) {
        Class<?> clazz = configuration.getClazz();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(mg.matsd.javaframework.core.annotations.ManagedInstance.class))
                continue;

            mg.matsd.javaframework.core.annotations.ManagedInstance m = method.getAnnotation(mg.matsd.javaframework.core.annotations.ManagedInstance.class);
            ManagedInstance managedInstance = new ManagedInstance(
                StringUtils.isBlank(m.value()) ? null : m.value(), method.getReturnType(),
                method.isAnnotationPresent(Scope.class) ? method.getAnnotation(Scope.class).value() : null,
                configuration, method
            );
            ManagedInstanceUtils.processConstructorArguments(method, managedInstance);

            managedInstanceDefinitionRegistry.registerManagedInstance(managedInstance);
        }
    }

    private static boolean isComponent(@Nullable Class<?> clazz) {
        if (clazz == null || clazz.isAnnotation()) return false;

        return AnnotationUtils.hasAnnotation(Component.class, clazz);
    }
}
