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

    ConstraintViolation(
        @Nullable Object invalidValue,
        Annotation annotation,
        Class<? extends Annotation> annotationType,
        ValidatorFactory validatorFactory
    ) {
        this.invalidValue   = invalidValue;
        this.annotation     = annotation;
        this.annotationType = annotationType;

        this.setMessageTemplate()
            .initMessageAndMessageParameters(validatorFactory);
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

    @Nullable
    public String getMessage() {
        return message;
    }

    public ConstraintViolation<T> setMessage(@Nullable String message) {
        this.message = message;
        return this;
    }

    @Nullable
    public Object getInvalidValue() {
        return invalidValue;
    }

    private void initMessageAndMessageParameters(ValidatorFactory validatorFactory) {
        if (messageTemplate == null) return;
        messageParameters = new HashMap<>();

        Matcher matcher = Pattern.compile("\\{\\{\\s*(.*?)\\s*}}").matcher(messageTemplate);
        StringBuilder stringBuilder = new StringBuilder();

        while (matcher.find()) {
            String messageParameterName  = matcher.group(1);
            Object messageParameterValue = getMessageParameterValue(messageParameterName, validatorFactory);
            messageParameters.put(messageParameterName, messageParameterValue);

            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(String.valueOf(messageParameterValue)));
        }
        matcher.appendTail(stringBuilder);

        message = stringBuilder.toString();
    }

    @Nullable
    private Object getMessageParameterValue(String messageParameterName, ValidatorFactory validatorFactory) {
        if ("value".equals(messageParameterName))
            return invalidValue;

        if (messageParameterName.startsWith(ValidatorFactory.MATSD_VALIDATION_MESSAGE_PREFIX))
            return validatorFactory.getDefaultMessage(messageParameterName);

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
            ", messageTemplate='" + messageTemplate + '\'' +
            ", annotationType=" + annotationType +
            '}';
    }
}
