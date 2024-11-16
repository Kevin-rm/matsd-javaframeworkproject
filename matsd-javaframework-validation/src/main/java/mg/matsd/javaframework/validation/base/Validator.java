package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.annotations.Constraint;
import mg.matsd.javaframework.validation.exceptions.ValidationProcessException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.IntStream;

public class Validator {
    private final ValidatorFactory validatorFactory;
    private final Map<Class<?>, Field[]> fieldsCache;

    Validator(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
        fieldsCache = new HashMap<>();
    }

    public ValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    public <T> ValidationErrors<T> doValidate(T t, @Nullable Class<?>... groups) {
        Assert.notNull(t, "L'objet à valider ne peut pas être \"null\"");

        ValidationErrors<T> validationErrors = new ValidationErrors<>(t);
        for (Field field : getFields(t.getClass())) {
            field.setAccessible(true);

            for (Annotation annotation : field.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                ConstraintValidator<Annotation, Object>[] constraintValidators = getConstraintValidators(annotationType);
                if (constraintValidators == null) continue;

                try {
                    Object fieldValue = field.get(t);
                    for (ConstraintValidator<Annotation, Object> constraintValidator : constraintValidators) {
                        constraintValidator.initialize(annotation);
                        boolean isValid = constraintValidator.isValid(fieldValue);

                        if (isValid || !isInGroups(annotation, annotationType, groups)) continue;

                        validationErrors.addConstraintViolation(field.getName(),
                            new ConstraintViolation<>(fieldValue, annotation, annotationType, validatorFactory));
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
    private ConstraintValidator<Annotation, Object>[] getConstraintValidators(Class<? extends Annotation> annotationType) {
        Class<? extends ConstraintValidator<?, ?>>[] constraintValidatorClasses = annotationType.getAnnotation(Constraint.class)
            .validatedBy();

        final int length = constraintValidatorClasses.length;
        if (length == 0) return null;

        ConstraintValidatorFactory constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
        return IntStream.range(0, length).mapToObj(
                i -> (ConstraintValidator<Annotation, Object>) constraintValidatorFactory.getInstance(constraintValidatorClasses[i])
            ).toArray(ConstraintValidator[]::new);
    }

    private boolean isInGroups(Annotation annotation, Class<? extends Annotation> annotationType, @Nullable Class<?>[] groups) {
        if (groups == null || groups.length == 0) return true;

        final Class<?>[] annotationGroups;
        try {
            annotationGroups = (Class<?>[]) annotationType.getMethod("groups").invoke(annotation);
        } catch (Exception ignored) { return true; }

        return Arrays.stream(groups)
            .filter(Objects::nonNull)
            .anyMatch(group -> Arrays.stream(annotationGroups)
                .filter(Objects::nonNull)
                .anyMatch(annotationGroup -> annotationGroup.isAssignableFrom(group))
            );
    }
}
