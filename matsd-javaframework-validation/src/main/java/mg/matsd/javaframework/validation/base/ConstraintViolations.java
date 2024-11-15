package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.*;

public class ConstraintViolations<T> {
    private final Map<String, List<ConstraintViolation<T>>> constraintViolationMap;

    ConstraintViolations() {
        constraintViolationMap = new HashMap<>();
    }

    void addConstraintViolation(String property, ConstraintViolation<T> constraintViolation) {
        List<ConstraintViolation<T>> constraintViolations = hasConstraintViolations(property) ?
            constraintViolationMap.get(property) : Collections.unmodifiableList(new ArrayList<>());

        constraintViolations.add(constraintViolation);
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

    public Map<String, List<ConstraintViolation<T>>> getConstraintViolationMap() {
        return constraintViolationMap;
    }
}
