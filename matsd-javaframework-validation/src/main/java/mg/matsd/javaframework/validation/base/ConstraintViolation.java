package mg.matsd.javaframework.validation.base;

import java.util.Map;

public class ConstraintViolation<T> {
    private String messageTemplate;
    private Map<String, Object> messageParameters;
    private String message;
    private T validatedObject;
    private Object invalidValue;

    ConstraintViolation(String messageTemplate, T validatedObject, Object invalidValue) {
        this.setMessageTemplate(messageTemplate)
            .setValidatedObject(validatedObject)
            .setInvalidValue(invalidValue);
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public ConstraintViolation<T> setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
        return this;
    }

    public Map<String, Object> getMessageParameters() {
        return messageParameters;
    }

    public ConstraintViolation<T> setMessageParameters(Map<String, Object> messageParameters) {
        this.messageParameters = messageParameters;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ConstraintViolation<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getValidatedObject() {
        return validatedObject;
    }

    public ConstraintViolation<T> setValidatedObject(T validatedObject) {
        this.validatedObject = validatedObject;
        return this;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    public ConstraintViolation<T> setInvalidValue(Object invalidValue) {
        this.invalidValue = invalidValue;
        return this;
    }
}
