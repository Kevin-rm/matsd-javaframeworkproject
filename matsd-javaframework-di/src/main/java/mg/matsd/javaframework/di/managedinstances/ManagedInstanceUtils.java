package mg.matsd.javaframework.di.managedinstances;

import mg.matsd.javaframework.di.annotations.Identifier;
import mg.matsd.javaframework.di.annotations.Inject;
import mg.matsd.javaframework.di.managedinstances.factory.ManagedInstanceFactory;
import mg.matsd.javaframework.core.utils.Assert;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class ManagedInstanceUtils {
    private ManagedInstanceUtils() { }

    public static Constructor<?> constructorToUse(ManagedInstance managedInstance) {
        Assert.notNull(managedInstance);

        Constructor<?>[] constructors = getCandidateConstructors(managedInstance);
        if (constructors.length == 1) return constructors[0];
        Arrays.sort(constructors, (c1, c2) -> Integer.compare(c2.getParameterCount(), c1.getParameterCount()));

        return constructors[0];
    }

    public static void addConstructorArguments(Executable executable, ManagedInstance managedInstance) {
        Assert.notNull(executable);
        Assert.notNull(managedInstance);

        Parameter[] parameters = executable.getParameters();
        IntStream.range(0, parameters.length).forEachOrdered(i -> {
            Parameter parameter = parameters[i];
            managedInstance.addConstructorArgument(i, parameter.getType(), parameter.isAnnotationPresent(Identifier.class) ?
                parameter.getAnnotation(Identifier.class).value() : null);
        });
    }

    public static Object instantiate(ManagedInstance managedInstance, ManagedInstanceFactory managedInstanceFactory) {
        Method factoryMethod = managedInstance.getFactoryMethod();
        if (factoryMethod != null)
            return instanceFromFactoryMethod(factoryMethod, managedInstance, managedInstanceFactory);

        Object instance;
        try {
            Constructor<?> constructor = constructorToUse(managedInstance);
            instance = constructor.newInstance(resolveConstructorArguments(constructor, managedInstance));

            for (Property property : managedInstance.getProperties()) {
                Field field = property.getField();
                field.setAccessible(true);

                field.set(instance, property.getValue());
            }
        } catch (IllegalAccessException e) {
            throw new ManagedInstanceException(String.format("Le constructeur de la \"ManagedInstance\" " +
                "avec l'identifiant \"%s\" n'est pas accessible", managedInstance.getId()));
        } catch (InstantiationException e) {
            throw new ManagedInstanceException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }

        return instance;
    }

    private static Object instanceFromFactoryMethod(
        Method factoryMethod,
        ManagedInstance managedInstance,
        ManagedInstanceFactory managedInstanceFactory
    ) {
        try {
            return factoryMethod.invoke(
                managedInstanceFactory.getManagedInstance(managedInstance.getParent().getClazz()),
                resolveConstructorArguments(factoryMethod, managedInstance)
            );
        } catch (IllegalAccessException e) {
            throw new ManagedInstanceException(String.format(
                "La \"factoryMethod\" \"%s\" de la \"ManagedInstance\" avec l'identifiant \"%s\" n'est pas accessible",
                factoryMethod, managedInstance.getId()
            ));
        } catch (InvocationTargetException e) {
            throw new ManagedInstanceException(e.getCause());
        }
    }

    private static Object[] resolveConstructorArguments(Executable executable, ManagedInstance managedInstance) {
        Object[] args = new Object[executable.getParameterCount()];
        managedInstance.getConstructorArguments()
            .forEach(constructorArgument -> args[constructorArgument.getIndex()] = constructorArgument.getValue());

        return args;
    }

    private static Constructor<?>[] getCandidateConstructors(ManagedInstance managedInstance) {
        Constructor<?>[] constructors = managedInstance.getClazz().getDeclaredConstructors();
        List<Constructor<?>> requestedConstructors = Arrays.stream(constructors)
            .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
            .toList();

        return requestedConstructors.isEmpty() ? constructors : requestedConstructors.toArray(new Constructor<?>[0]);
    }
}
