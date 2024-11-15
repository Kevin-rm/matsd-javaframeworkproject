package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstraintViolation<T> {
    private static final String MATSD_VALIDATION_MESSAGE_PREFIX = "mg.matsd.javaframework.validation.constraints";

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
            .initMessageAndMessageParameters();
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

    @Nullable
    public Object getInvalidValue() {
        return invalidValue;
    }

    private void initMessageAndMessageParameters() {
        if (messageTemplate == null) return;
        messageParameters = new HashMap<>();

        Pattern pattern = Pattern.compile("\\{\\{\\s*(.*?)\\s*}}");
        Matcher matcher = pattern.matcher(messageTemplate);
        StringBuilder stringBuilder = new StringBuilder();

        while (matcher.find()) {
            String messageParameterName  = matcher.group(1);
            Object messageParameterValue = getMessageParameterValue(messageParameterName);
            messageParameters.put(messageParameterName, messageParameterValue);

            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(String.valueOf(messageParameterValue)));
        }
        matcher.appendTail(stringBuilder);

        message = stringBuilder.toString();
    }

    @Nullable
    private Object getMessageParameterValue(String messageParameterName) {
        if ("value".equals(messageParameterName))
            return invalidValue;

        if (messageParameterName.startsWith(MATSD_VALIDATION_MESSAGE_PREFIX))
            return ValidatorFactory.getMessage(messageParameterName);

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
