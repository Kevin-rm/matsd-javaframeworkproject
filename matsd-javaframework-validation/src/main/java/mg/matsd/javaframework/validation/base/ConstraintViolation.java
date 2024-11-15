package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstraintViolation<T> {
    @Nullable
    private String messageTemplate;
    @Nullable
    private Map<String, Object> messageParameters;
    @Nullable
    private String message;
    @Nullable
    private final Object invalidValue;
    private final Annotation annotation;
    private final Class<? extends Annotation> annotationType;

    ConstraintViolation(@Nullable Object invalidValue, Annotation annotation, Class<? extends Annotation> annotationType) {
        this.invalidValue   = invalidValue;
        this.annotation     = annotation;
        this.annotationType = annotationType;

        this.setMessageTemplate()
            .setMessageParameters()
            .setMessage();
    }

    @Nullable
    public String getMessageTemplate() {
        return messageTemplate;
    }

    private ConstraintViolation<T> setMessageTemplate() {
        try {
            messageTemplate = (String) annotationType.getMethod("message").invoke(annotation);
        } catch (Exception ignored) { }

        return this;
    }

    @Nullable
    public Map<String, Object> getMessageParameters() {
        return messageParameters;
    }

    private ConstraintViolation<T> setMessageParameters() {
        if (messageTemplate == null) return this;
        messageParameters = new HashMap<>();

        Pattern pattern = Pattern.compile("\\{\\{\\s*(.*?)\\s*}}");
        Matcher matcher = pattern.matcher(messageTemplate);
        while (matcher.find()) {
            String messageParameterName = matcher.group(1);
            messageParameters.put(messageParameterName, getMessageParameterValue(messageParameterName));
        }

        return this;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    private ConstraintViolation<T> setMessage() {
        if (messageParameters == null) return this;

        message = messageTemplate;
        messageParameters.forEach((key, value) ->
            message = message.replaceFirst(String.format("\\{\\{\\s*%s\\s*\\}\\}", key), String.valueOf(value))
        );

        return this;
    }

    @Nullable
    public Object getInvalidValue() {
        return invalidValue;
    }

    @Nullable
    private Object getMessageParameterValue(String messageParameterName) {
        if ("value".equals(messageParameterName))
            return invalidValue;

        try {
            return annotationType.getMethod(messageParameterName).invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ConstraintViolation{" +
            "message='" + message + '\'' +
            ", invalidValue=" + invalidValue +
            '}';
    }
}
