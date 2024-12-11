package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.annotations.Identifier;
import mg.matsd.javaframework.core.managedinstances.factory.ManagedInstanceFactory;
import mg.matsd.javaframework.core.utils.Assert;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ManagedInstanceUtils {
    private ManagedInstanceUtils() { }

    public static Constructor<?> constructorToUse(ManagedInstance managedInstance) {
        Assert.notNull(managedInstance);

        Constructor<?>[] constructors = managedInstance.getClazz().getDeclaredConstructors();
        if (constructors.length == 1) return constructors[0];
        Arrays.sort(constructors, (c1, c2) -> Integer.compare(c2.getParameterCount(), c1.getParameterCount()));

        return constructors[0];
    }

    public static void processConstructorArguments(Executable executable, ManagedInstance managedInstance) {
        Assert.notNull(executable);
        Assert.notNull(managedInstance);

        Parameter[] parameters = executable.getParameters();
        IntStream.range(0, parameters.length).forEachOrdered(i -> {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();

            if (parameter.isAnnotationPresent(Identifier.class))
                managedInstance.addConstructorArgument(i, parameterType, parameter.getAnnotation(Identifier.class).value());
            else managedInstance.addConstructorArgument(i, parameterType, null);
        });
    }

    public static Object instantiate(ManagedInstance managedInstance, ManagedInstanceFactory managedInstanceFactory) {
        Method factoryMethod = managedInstance.getFactoryMethod();
        if (factoryMethod != null)
            return instanceFromFactoryMethod(factoryMethod, managedInstance, managedInstanceFactory);

        Object instance;
        try {
            Constructor<?> constructor = constructorToUse(managedInstance);
            instance = constructor.newInstance(getConstructorArguments(constructor, managedInstance));

            for (Property property : managedInstance.getProperties()) {
                Field field = property.getField();
                field.setAccessible(true);

                field.set(instance, property.getValue());
            }
        } catch (IllegalAccessException e) {
            throw new ManagedInstanceException(String.format("Le constructeur de la \"ManagedInstance\" " +
                "avec l'identifiant \"%s\" n'est pas accessible", managedInstance.getId()));
        } catch (InvocationTargetException | InstantiationException e) {
            throw new ManagedInstanceException(e);
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
                getConstructorArguments(factoryMethod, managedInstance)
            );
        } catch (IllegalAccessException e) {
            throw new ManagedInstanceException(String.format(
                "La \"factoryMethod\" \"%s\" de la \"ManagedInstance\" avec l'identifiant \"%s\" n'est pas accessible",
                factoryMethod, managedInstance.getId()
            ));
        } catch (InvocationTargetException e) {
            throw new ManagedInstanceException(e);
        }
    }

    private static Object[] getConstructorArguments(Executable executable, ManagedInstance managedInstance) {
        Object[] args = new Object[executable.getParameterCount()];
        for (ConstructorArgument constructorArgument : managedInstance.getConstructorArguments())
            args[constructorArgument.getIndex()] = constructorArgument.getValue();

        return args;
    }
}
