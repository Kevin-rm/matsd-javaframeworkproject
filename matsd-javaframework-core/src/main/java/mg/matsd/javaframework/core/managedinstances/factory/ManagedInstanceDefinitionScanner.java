package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.annotations.*;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.ClassScanner;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.IntStream;

class ManagedInstanceDefinitionScanner {
    private ManagedInstanceDefinitionScanner() { }

    static void doScanComponents(ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry, String packageName) {
        ClassScanner.doScan(packageName, clazz -> {
            if (!isComponent(clazz)) return;

            Component component = (Component) AnnotationUtils.getAnnotation(Component.class, clazz);
            String scope = null;
            if (clazz.isAnnotationPresent(Scope.class))
                scope = clazz.getAnnotation(Scope.class).value();

            ManagedInstance managedInstance = new ManagedInstance(
                StringUtils.isBlank(component.value()) ? null : component.value(),
                clazz, scope, null, null
            );
            processConstructorArguments(ManagedInstanceUtils.constructorToUse(managedInstance), managedInstance);

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
            String scope = null;
            if (method.isAnnotationPresent(Scope.class))
                scope = method.getAnnotation(Scope.class).value();

            ManagedInstance managedInstance = new ManagedInstance(
                StringUtils.isBlank(m.value()) ? null : m.value(),
                method.getReturnType(), scope, configuration, method
            );
            processConstructorArguments(method, managedInstance);

            managedInstanceDefinitionRegistry.registerManagedInstance(managedInstance);
        }
    }

    private static void processConstructorArguments(Executable executable, ManagedInstance managedInstance) {
        Parameter[] parameters = executable.getParameters();
        IntStream.range(0, parameters.length).forEachOrdered(i -> {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();

            if (parameter.isAnnotationPresent(Identifier.class))
                 managedInstance.addConstructorArgument(i, parameterType, parameter.getAnnotation(Identifier.class).value());
            else managedInstance.addConstructorArgument(i, parameterType, null);
        });
    }

    private static boolean isComponent(@Nullable Class<?> clazz) {
        if (clazz == null || clazz.isAnnotation()) return false;

        return AnnotationUtils.hasAnnotation(Component.class, clazz);
    }
}
