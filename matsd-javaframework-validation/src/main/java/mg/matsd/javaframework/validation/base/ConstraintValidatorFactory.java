package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.validation.exceptions.ValidationProcessException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ConstraintValidatorFactory {
    private final Map<Class<?>, Object> instances;

    ConstraintValidatorFactory() {
        instances = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> clazz) {
        return (T) instances.computeIfAbsent(clazz, k -> {
            try {
                return k.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new ValidationProcessException(e);
            } catch (InvocationTargetException e) {
                throw new ValidationProcessException(e.getCause());
            }
        });
    }

    void releaseInstance(ConstraintValidator<?, ?> instance) {
        if (instance == null) return;
        instances.values().removeIf(existingInstance -> existingInstance == instance);
    }
}
