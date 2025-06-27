package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors<T> {
    private final Map<String, List<ConstraintViolation<T>>> constraintViolationMap;
    private final T validatedObject;

    ValidationErrors(T validatedObject) {
        constraintViolationMap = new HashMap<>();
        this.validatedObject = validatedObject;
    }

    public Map<String, List<ConstraintViolation<T>>> getConstraintViolationMap() {
        return constraintViolationMap;
    }

    public T getValidatedObject() {
        return validatedObject;
    }

    public boolean any() {
        return !constraintViolationMap.isEmpty();
    }

    public int count() {
        return constraintViolationMap.size();
    }

    public boolean hasConstraintViolations(String property) {
        Assert.notBlank(property, false, "Le nom de propriété (champ) ne peut pas être vide ou \"null\"");

        return constraintViolationMap.containsKey(property);
    }

    @Nullable
    public List<ConstraintViolation<T>> getConstraintViolations(String property) {
        Assert.notBlank(property, false, "Le nom de propriété (champ) ne peut pas être vide ou \"null\"");

        return constraintViolationMap.get(property);
    }

    @SuppressWarnings("unchecked")
    public void throwExceptionIfAny() throws ValidationException {
        if (constraintViolationMap.isEmpty()) return;

        throw new ValidationException((Map<String, List<ConstraintViolation<?>>>) (Map<String, ?>) constraintViolationMap);
    }

    void addConstraintViolation(String property, ConstraintViolation<T> constraintViolation) {
        constraintViolationMap.computeIfAbsent(property, k -> new ArrayList<>())
            .add(constraintViolation);
    }

    @Override
    public String toString() {
        return "ValidationErrors{" +
            "constraintViolationMap=" + constraintViolationMap +
            ", validatedObject=" + validatedObject +
            '}';
    }
}
