package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;

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
    private final String property;
    @Nullable
    private final Object invalidValue;
    private final ConstraintMapping<?> constraintMapping;
    private final Annotation annotation;

    ConstraintViolation(
        String property,
        @Nullable Object invalidValue,
        ConstraintMapping<?> constraintMapping,
        Annotation annotation,
        ValidatorFactory validatorFactory
    ) {
        this.property          = property;
        this.invalidValue      = invalidValue;
        this.constraintMapping = constraintMapping;
        this.annotation        = annotation;

        this.setMessageTemplate()
            .initMessageAndMessageParameters(validatorFactory);
    }

    @Nullable
    public String getMessageTemplate() {
        return messageTemplate;
    }

    private ConstraintViolation<T> setMessageTemplate() {
        try {
            messageTemplate = (String) constraintMapping.getMessageMethod().invoke(annotation);
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

    public String getProperty() {
        return property;
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

        if ("property".equals(messageParameterName))
            return property;

        try {
            return constraintMapping.getAnnotationClass().getMethod(messageParameterName).invoke(annotation);
        } catch (Exception ignored) {
            if (validatorFactory.hasDefaultMessage(messageParameterName))
                 return validatorFactory.getDefaultMessage(messageParameterName);
            else return null;
        }
    }

    @Override
    public String toString() {
        return "ConstraintViolation{" +
            "message='" + message + '\'' +
            ", property='" + property + '\'' +
            ", invalidValue=" + invalidValue +
            ", messageTemplate='" + messageTemplate + '\'' +
            ", constraintMapping=" + constraintMapping +
            '}';
    }
}
