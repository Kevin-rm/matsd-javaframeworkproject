package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.exceptions.ValidationProcessException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Validator {
    private final Map<Class<? extends Annotation>, ConstraintValidator<Annotation, Object>[]> constraintValidatorsCache = new HashMap<>();
    private final Map<Class<?>, Field[]> fieldsCache = new HashMap<>();

    public <T> Set<ConstraintViolation<T>> doValidate(T t, @Nullable Class<?>... groups) {
        Assert.notNull(t, "L'objet à valider ne peut pas être \"null\"");

        Set<ConstraintViolation<T>> constraintViolations = new HashSet<>();
        for (Field field : getFields(t.getClass())) {
            field.setAccessible(true);

            for (Annotation annotation : field.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                ConstraintValidator<Annotation, Object>[] constraintValidators = getOrCreateConstraintValidators(annotationType);
                if (constraintValidators == null) continue;

                try {
                    Object fieldValue = field.get(t);
                    for (ConstraintValidator<Annotation, Object> constraintValidator : constraintValidators) {
                        constraintValidator.initialize(annotation);
                        boolean isValid = constraintValidator.isValid(fieldValue);

                        if (isValid) continue;

                        Class<?>[] annotationGroups = getAnnotationGroups(annotation, annotationType);
                        if (groups != null && groups.length > 0 && !isInGroups(annotationGroups, groups))
                            continue;

                        constraintViolations.add(new ConstraintViolation<>(
                            getAnnotationMessage(annotation, annotationType), t, fieldValue));
                    }
                } catch (IllegalAccessException e) {
                    throw new ValidationProcessException(e);
                }
            }
        }

        return constraintViolations;
    }

    private Field[] getFields(Class<?> clazz) {
        return fieldsCache.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private ConstraintValidator<Annotation, Object>[] getOrCreateConstraintValidators(Class<? extends Annotation> annotationType) {
        if (!annotationType.isAnnotationPresent(Constraint.class)) return null;

        ConstraintValidator<Annotation, Object>[] constraintValidators = constraintValidatorsCache.get(annotationType);
        if (constraintValidators == null) {
            Class<? extends ConstraintValidator<?, ?>>[] constraintValidatorClasses = annotationType.getAnnotation(Constraint.class)
                .validatedBy();

            final int constraintValidatorClassesLength = constraintValidatorClasses.length;
            if (constraintValidatorClassesLength == 0) return null;

            constraintValidators = new ConstraintValidator[constraintValidatorClassesLength];
            for (int i = 0; i < constraintValidatorClassesLength; i++)
                try {
                    constraintValidators[i] = (ConstraintValidator<Annotation, Object>) constraintValidatorClasses[i].getConstructor()
                        .newInstance();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new ValidationProcessException(e);
                } catch (InvocationTargetException e) {
                     throw new ValidationProcessException(e.getCause());
                }

            constraintValidatorsCache.put(annotationType, constraintValidators);
        }

        return constraintValidators;
    }

    @Nullable
    private static String getAnnotationMessage(Annotation annotation, Class<? extends Annotation> annotationType) {
        try {
            return (String) annotationType.getMethod("message").invoke(annotation);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static Class<?>[] getAnnotationGroups(Annotation annotation, Class<? extends Annotation> annotationType) {
        try {
            return (Class<?>[]) annotationType.getMethod("groups").invoke(annotation);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isInGroups(Class<?>[] annotationGroups, Class<?>[] groups) {
        return Arrays.stream(groups).anyMatch(
            group -> Arrays.stream(annotationGroups).anyMatch(
                annotationGroup -> annotationGroup.isAssignableFrom(group)
            ));
    }
}
