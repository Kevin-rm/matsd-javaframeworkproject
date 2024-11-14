package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class Validator {

    public <T> Set<ConstraintViolation<T>> doValidate(T t, @Nullable Class<?>... groups) {
        Assert.notNull(t, "L'objet à valider ne peut pas être \"null\"");

        Set<ConstraintViolation<T>> constraintViolationSet = new HashSet<>();

        for (Field field : t.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            for (Annotation annotation : field.getAnnotations()) {
                ConstraintValidator<Annotation, Object> constraintValidator = getValidator(annotation);
                if (constraintValidator == null) continue;

                try {
                    Object fieldValue = field.get(t);
                    boolean isValid = constraintValidator.isValid(fieldValue);

                    if (isValid) continue;
                    constraintViolationSet.add(new ConstraintViolation<>(null, t, fieldValue));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return constraintViolationSet;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static ConstraintValidator<Annotation, Object> getValidator(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (!annotationType.isAnnotationPresent(Constraint.class)) return null;

        Class<? extends ConstraintValidator<?, ?>>[] constraintValidatorClasses = annotationType.getAnnotation(Constraint.class)
            .validatedBy();
        if (constraintValidatorClasses.length == 0) return null;

        try {
            return (ConstraintValidator<Annotation, Object>) constraintValidatorClasses[0].getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
