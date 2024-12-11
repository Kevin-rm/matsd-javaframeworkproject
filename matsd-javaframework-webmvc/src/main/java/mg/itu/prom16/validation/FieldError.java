package mg.itu.prom16.validation;

import mg.matsd.javaframework.validation.base.ConstraintViolation;

public class FieldError {
    private final String modelName;
    private final String property;
    private final String message;
    private final Object rejectedValue;

    FieldError(final String modelName, final ConstraintViolation<?> constraintViolation) {
        this.modelName     = modelName;
        this.property      = constraintViolation.getProperty();
        this.message       = constraintViolation.getMessage();
        this.rejectedValue = constraintViolation.getInvalidValue();
    }

    public String getModelName() {
        return modelName;
    }

    public String getProperty() {
        return property;
    }

    public String getMessage() {
        return message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    @Override
    public String toString() {
        return "FieldError{" +
            "modelName='" + modelName + '\'' +
            ", property='" + property + '\'' +
            ", message='" + message + '\'' +
            ", rejectedValue=" + rejectedValue +
            '}';
    }
}
