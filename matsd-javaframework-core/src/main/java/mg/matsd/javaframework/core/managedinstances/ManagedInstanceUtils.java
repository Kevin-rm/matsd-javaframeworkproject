package mg.matsd.javaframework.core.managedinstances;

import mg.matsd.javaframework.core.managedinstances.factory.ManagedInstanceFactory;

import java.lang.reflect.*;

public class ManagedInstanceUtils {
    private ManagedInstanceUtils() { }

    public static Object instantiate(ManagedInstance managedInstance, ManagedInstanceFactory managedInstanceFactory) {
        Method factoryMethod = managedInstance.getFactoryMethod();
        if (factoryMethod != null)
            return createInstanceFromFactoryMethod(factoryMethod, managedInstance, managedInstanceFactory);

        Object instance;
        try {
            Constructor<?> constructor = managedInstance.findSuitableConstructor();
            if (constructor == null) throw new NoSuchMethodException();

            Object[] initArgs = new Object[constructor.getParameterCount()];
            for (ConstructorArgument constructorArgument : managedInstance.getConstructorArguments())
                initArgs[constructorArgument.getIndex()] = constructorArgument.getValue();

            instance = constructor.newInstance(initArgs);

            for (Property property : managedInstance.getProperties()) {
                Field field = property.getField();
                field.setAccessible(true);

                field.set(instance, property.getValue());
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new ManagedInstanceException(String.format("Aucun constructeur approprié et accessible trouvé " +
                "pour la \"ManagedInstance\" avec l'identifiant \"%s\"", managedInstance.getId()));
        } catch (InvocationTargetException | InstantiationException e) {
            throw new ManagedInstanceException(e);
        }

        return instance;
    }

    private static Object createInstanceFromFactoryMethod(
        Method factoryMethod,
        ManagedInstance managedInstance,
        ManagedInstanceFactory managedInstanceFactory
    ) {
        try {
            Object[] args = new Object[factoryMethod.getParameterCount()];
            for (ConstructorArgument constructorArgument : managedInstance.getConstructorArguments())
                args[constructorArgument.getIndex()] = constructorArgument.getValue();

            return factoryMethod.invoke(
                managedInstanceFactory.getManagedInstance(factoryMethod.getDeclaringClass()), args
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
}
