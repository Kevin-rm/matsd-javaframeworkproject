package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.*;

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

    public boolean isEmpty() {
        return constraintViolationMap.isEmpty();
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

    void addConstraintViolation(String property, ConstraintViolation<T> constraintViolation) {
        constraintViolationMap.computeIfAbsent(property, k -> new ArrayList<>())
            .add(constraintViolation);
    }
}
