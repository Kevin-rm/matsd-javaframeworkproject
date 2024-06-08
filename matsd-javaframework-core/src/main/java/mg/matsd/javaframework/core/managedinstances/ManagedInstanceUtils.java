package mg.matsd.javaframework.core.managedinstances;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ManagedInstanceUtils {
    private ManagedInstanceUtils() { }

    public static Object instantiate(ManagedInstance managedInstance) {
        Object instance = null;
        try {
            Constructor<?> constructor = managedInstance.getClazz().getConstructor();
            instance = constructor.newInstance();

            for (Property property : managedInstance.getProperties()) {
                Field field = property.getField();
                field.setAccessible(true);

                field.set(instance, property.getValue());
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new ManagedInstanceException("La classe d'une \"ManagedInstance\" doit avoir un constructeur sans arguments et accessible (non privé)");
        } catch (InvocationTargetException | InstantiationException e) {
            throw new ManagedInstanceException(e);
        }

        return instance;
    }
}
