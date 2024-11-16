package mg.matsd.javaframework.validation.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.base.ConstraintViolation;

import java.util.List;
import java.util.Map;

public class ValidationException extends BaseException {
    private static final String PREFIX = "Erreur de validation de données";

    public <T> ValidationException(Map<String, List<ConstraintViolation<T>>> constraintViolationMap) {
        super(createMessage(constraintViolationMap), PREFIX);
    }

    private static <T> String createMessage(Map<String, List<ConstraintViolation<T>>> constraintViolationMap) {
        Assert.notNull(constraintViolationMap);

        StringBuilder stringBuilder = new StringBuilder();
        constraintViolationMap.forEach((property, violations) -> {
            stringBuilder.append("\n. Propriété : ").append(property).append("\n");
            violations.forEach(violation -> {
                stringBuilder.append("\t- ").append(violation.getMessage()).append("\n");
            });
        });

        return stringBuilder.toString();
    }
}
