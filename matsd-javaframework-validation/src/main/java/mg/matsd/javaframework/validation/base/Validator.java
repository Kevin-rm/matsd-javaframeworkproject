package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.exceptions.ValidationProcessException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Validator {
    private final Map<Class<? extends Annotation>, ConstraintValidator<Annotation, Object>[]> constraintValidatorsCache;
    private final Map<Class<?>, Field[]> fieldsCache;

    Validator() {
        constraintValidatorsCache = new HashMap<>();
        fieldsCache = new HashMap<>();
    }

    public <T> ValidationErrors<T> doValidate(T t, @Nullable Class<?>... groups) {
        Assert.notNull(t, "L'objet à valider ne peut pas être \"null\"");

        ValidationErrors<T> validationErrors = new ValidationErrors<>(t);
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

                        if (isValid || !isInGroups(annotation, annotationType, groups)) continue;

                        validationErrors.addConstraintViolation(field.getName(),
                            new ConstraintViolation<>(fieldValue, annotation, annotationType));
                    }
                } catch (IllegalAccessException e) {
                    throw new ValidationProcessException(e);
                }
            }
        }

        return validationErrors;
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
                    constraintValidators[i] = (ConstraintValidator<Annotation, Object>) constraintValidatorClasses[i]
                        .getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new ValidationProcessException(e);
                } catch (InvocationTargetException e) {
                     throw new ValidationProcessException(e.getCause());
                }

            constraintValidatorsCache.put(annotationType, constraintValidators);
        }

        return constraintValidators;
    }

    private boolean isInGroups(Annotation annotation, Class<? extends Annotation> annotationType, @Nullable Class<?>[] groups) {
        if (groups == null || groups.length == 0) return true;

        Class<?>[] annotationGroups;
        try {
            annotationGroups = (Class<?>[]) annotationType.getMethod("groups").invoke(annotation);
        } catch (Exception ignored) {
            return true;
        }

        Class<?>[] finalAnnotationGroups = annotationGroups;
        return Arrays.stream(groups)
            .filter(Objects::nonNull)
            .anyMatch(group -> Arrays.stream(finalAnnotationGroups)
                .filter(Objects::nonNull).anyMatch(
                    annotationGroup -> annotationGroup.isAssignableFrom(group)
                ));
    }
}
