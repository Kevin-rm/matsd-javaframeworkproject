package mg.itu.prom16.validation;

import mg.matsd.javaframework.validation.base.ConstraintViolation;

public class FieldError {
    private final String message;
    private final String property;
    private final Object invalidValue;

    FieldError(final ConstraintViolation<?> constraintViolation) {
        this.message      = constraintViolation.getMessage();
        this.property     = constraintViolation.getProperty();
        this.invalidValue = constraintViolation.getInvalidValue();
    }

    public String getMessage() {
        return message;
    }

    public String getProperty() {
        return property;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    @Override
    public String toString() {
        return "FieldError{" +
            "message='" + message + '\'' +
            ", property='" + property + '\'' +
            ", invalidValue=" + invalidValue +
            '}';
    }
}
