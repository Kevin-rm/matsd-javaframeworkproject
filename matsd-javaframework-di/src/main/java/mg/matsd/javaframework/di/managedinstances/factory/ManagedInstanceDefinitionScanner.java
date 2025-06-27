package mg.matsd.javaframework.di.managedinstances.factory;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.ClassScanner;
import mg.matsd.javaframework.core.utils.ClassUtils;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.di.annotations.*;
import mg.matsd.javaframework.di.managedinstances.ManagedInstance;
import mg.matsd.javaframework.di.managedinstances.ManagedInstanceUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

class ManagedInstanceDefinitionScanner {
    private ManagedInstanceDefinitionScanner() { }

    static void doScanComponents(ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry, String packageName) {
        ClassScanner.doScan(packageName, clazz -> {
            if (!isComponent(clazz) || shouldNotProcessElement(clazz)) return;

            String componentValue = ((Component) AnnotationUtils.getAnnotation(Component.class, clazz)).value();
            ManagedInstance managedInstance = new ManagedInstance(
                StringUtils.isBlank(componentValue) ? null : componentValue, clazz,
                clazz.isAnnotationPresent(Scope.class) ? clazz.getAnnotation(Scope.class).value() : null,
                clazz.isAnnotationPresent(Lazy.class)  ? "true" : null,
                null, null);

            ManagedInstanceUtils.addConstructorArguments(ManagedInstanceUtils.constructorToUse(managedInstance), managedInstance);
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
            if (!method.isAnnotationPresent(mg.matsd.javaframework.di.annotations.ManagedInstance.class) ||
                shouldNotProcessElement(method)) continue;

            String value = method.getAnnotation(mg.matsd.javaframework.di.annotations.ManagedInstance.class).value();
            ManagedInstance managedInstance = new ManagedInstance(
                StringUtils.isBlank(value) ? null : value, method.getReturnType(),
                method.isAnnotationPresent(Scope.class) ? method.getAnnotation(Scope.class).value() : null,
                method.isAnnotationPresent(Lazy.class)  ? "true" : null,
                configuration, method);
            ManagedInstanceUtils.addConstructorArguments(method, managedInstance);

            managedInstanceDefinitionRegistry.registerManagedInstance(managedInstance);
        }
    }

    private static boolean shouldNotProcessElement(AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(IfClassPresent.class))
            for (String className : annotatedElement.getAnnotation(IfClassPresent.class).value())
                if (ClassUtils.isClassMissing(className)) return true;

        if (annotatedElement.isAnnotationPresent(IfClassMissing.class))
            for (String className : annotatedElement.getAnnotation(IfClassMissing.class).value())
                if (ClassUtils.isClassPresent(className)) return true;

        return false;
    }

    private static boolean isComponent(@Nullable Class<?> clazz) {
        if (clazz == null || clazz.isAnnotation()) return false;

        return AnnotationUtils.hasAnnotation(Component.class, clazz);
    }
}
