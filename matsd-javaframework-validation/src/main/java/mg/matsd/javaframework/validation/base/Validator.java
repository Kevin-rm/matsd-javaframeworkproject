package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.annotations.Constraint;
import mg.matsd.javaframework.validation.exceptions.ValidationProcessException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class Validator {
    private final ValidatorFactory validatorFactory;
    private final Map<Class<?>, Map<Field, Annotation[]>> fieldsCache;

    Validator(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
        fieldsCache = new HashMap<>();
    }

    public ValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> ValidationErrors<T> doValidate(T t, @Nullable Class<?>... groups) {
        Assert.notNull(t, "L'objet à valider ne peut pas être \"null\"");

        ValidationErrors<T> validationErrors = new ValidationErrors<>(t);
        getFields(t.getClass()).forEach((field, value) -> {
            field.setAccessible(true);

            Arrays.stream(value).forEachOrdered(annotation -> {
                ConstraintMapping<?> constraintMapping = validatorFactory.getConstraintMapping(annotation.annotationType());
                ConstraintValidator<Annotation, Object>[] constraintValidators = (ConstraintValidator<Annotation, Object>[])
                    getConstraintValidators(constraintMapping);

                try {
                    Object fieldValue = field.get(t);
                    Arrays.stream(constraintValidators).forEachOrdered(constraintValidator -> {
                        constraintValidator.initialize(annotation);
                        boolean isValid = constraintValidator.isValid(fieldValue);

                        if (isValid || !isInGroups(constraintMapping, annotation, groups)) return;

                        String fieldName = field.getName();
                        validationErrors.addConstraintViolation(fieldName,
                            new ConstraintViolation<>(fieldName, fieldValue, constraintMapping, annotation, validatorFactory));
                    });
                } catch (IllegalAccessException e) {
                    throw new ValidationProcessException(e);
                }
            });
        });

        return validationErrors;
    }

    private Map<Field, Annotation[]> getFields(Class<?> clazz) {
        if (fieldsCache.containsKey(clazz)) return fieldsCache.get(clazz);

        Map<Field, Annotation[]> fieldsMap = new HashMap<>();
        Arrays.stream(clazz.getDeclaredFields()).forEachOrdered(field -> {
            List<Annotation> annotations = new ArrayList<>();
            Arrays.stream(field.getAnnotations()).forEachOrdered(annotation -> {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.isAnnotationPresent(Constraint.class)) {
                    annotations.add(annotation);

                    if (validatorFactory.isAutoDetectConstraints())
                        validatorFactory.addConstraintMapping(annotationType);
                }
            });

            if (annotations.isEmpty()) return;
            fieldsMap.put(field, annotations.toArray(new Annotation[0]));
        });

        fieldsCache.put(clazz, fieldsMap);
        return fieldsMap;
    }

    private ConstraintValidator<?, ?>[] getConstraintValidators(ConstraintMapping<?> constraintMapping) {
        return Arrays.stream(constraintMapping.getConstraintValidatorClasses())
            .map(validatorFactory::getConstraintValidatorInstance)
            .toArray(ConstraintValidator[]::new);
    }

    private boolean isInGroups(ConstraintMapping<?> constraintMapping, Annotation annotation, @Nullable Class<?>[] groups) {
        if (groups == null || groups.length == 0) return true;

        final Class<?>[] annotationGroups;
        try {
            annotationGroups = (Class<?>[]) constraintMapping.getGroupsMethod().invoke(annotation);
        } catch (Exception ignored) { return true; }

        return Arrays.stream(groups)
            .filter(Objects::nonNull)
            .anyMatch(group -> Arrays.stream(annotationGroups)
                .filter(Objects::nonNull)
                .anyMatch(annotationGroup -> annotationGroup.isAssignableFrom(group))
            );
    }
}
