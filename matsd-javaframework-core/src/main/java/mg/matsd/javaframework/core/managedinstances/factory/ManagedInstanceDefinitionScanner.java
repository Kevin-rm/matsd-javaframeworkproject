package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.annotations.Component;
import mg.matsd.javaframework.core.annotations.Configuration;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.annotations.Scope;
import mg.matsd.javaframework.core.exceptions.PackageNotFoundException;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.utils.AnnotationUtils;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

class ManagedInstanceDefinitionScanner {
    private ManagedInstanceDefinitionScanner() { }

    static void doScanComponents(ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry, String packageName) {
        URL url = Thread.currentThread()
            .getContextClassLoader()
            .getResource(packageName.replace('.', '/'));

        if (url == null) throw new PackageNotFoundException(packageName);

        File[] files = new File(url.getPath()).listFiles();
        if (files == null) return;

        for (File file : files) {
            String fileName = file.getName();

            if (file.isDirectory()) doScanComponents(managedInstanceDefinitionRegistry, String.format("%s.%s", packageName, fileName));

            try {
                Class<?> clazz = Class.forName(
                    String.format("%s.%s", packageName, fileName.replaceAll("\\.class$", ""))
                );

                if (!isComponent(clazz)) continue;

                Component component = (Component) AnnotationUtils.getAnnotation(Component.class, clazz);
                String scope = null;
                if (clazz.isAnnotationPresent(Scope.class))
                    scope = clazz.getAnnotation(Scope.class).value();

                if (clazz.isAnnotationPresent(Configuration.class))
                    getManagedInstancesFromConfiguration(managedInstanceDefinitionRegistry, clazz);

                managedInstanceDefinitionRegistry.registerManagedInstance(new ManagedInstance(
                    StringUtils.isBlank(component.value()) ? null : component.value(),
                    clazz, scope, null
                ));
            } catch (ClassNotFoundException ignored) { }
        }
    }

    private static void getManagedInstancesFromConfiguration(
        ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry, Class<?> configurationClass
    ) {
        for (Method method : configurationClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(mg.matsd.javaframework.core.annotations.ManagedInstance.class))
                continue;

            mg.matsd.javaframework.core.annotations.ManagedInstance m = method.getAnnotation(mg.matsd.javaframework.core.annotations.ManagedInstance.class);
            String scope = null;
            if (method.isAnnotationPresent(Scope.class))
                scope = method.getAnnotation(Scope.class).value();

            ManagedInstance managedInstance = new ManagedInstance(
                StringUtils.isBlank(m.value()) ? null : m.value(),
                method.getReturnType(), scope, method
            );
            for (Class<?> parameterType : method.getParameterTypes())
                managedInstance.addConstructorArgument(null, null, parameterType.getName());

            managedInstanceDefinitionRegistry.registerManagedInstance(managedInstance);
        }
    }

    private static boolean isComponent(@Nullable Class<?> clazz) {
        if (clazz == null) return false;

        return AnnotationUtils.hasAnnotation(Component.class, clazz);
    }
}
