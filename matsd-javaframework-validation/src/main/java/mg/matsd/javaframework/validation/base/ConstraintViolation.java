package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.util.Map;

public class ConstraintViolation<T> {
    @Nullable
    private String message;
    @Nullable
    private Map<String, Object> messageParameters;
    @Nullable
    private final String messageTemplate;
    private final T validatedObject;
    private final Object invalidValue;

    ConstraintViolation(@Nullable String messageTemplate, T validatedObject, Object invalidValue) {
        this.messageTemplate = messageTemplate;
        this.validatedObject = validatedObject;
        this.invalidValue    = invalidValue;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    private ConstraintViolation<T> setMessage() {

        return this;
    }

    @Nullable
    public Map<String, Object> getMessageParameters() {
        return messageParameters;
    }

    private ConstraintViolation<T> setMessageParameters() {

        return this;
    }

    @Nullable
    public String getMessageTemplate() {
        return messageTemplate;
    }

    public T getValidatedObject() {
        return validatedObject;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
