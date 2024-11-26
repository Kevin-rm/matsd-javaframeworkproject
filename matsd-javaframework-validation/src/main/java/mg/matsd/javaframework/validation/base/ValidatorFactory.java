package mg.matsd.javaframework.validation.base;

import com.sun.jdi.InternalException;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.exceptions.ValidationProcessException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ValidatorFactory {
    private final Map<Class<?>, Object> constraintValidatorInstancesMap;
    private final List<ConstraintMapping<?>> constraintMappings;

    private Validator  validator;
    private boolean    autoDetectConstraints;
    private Properties defaultMessages;

    public ValidatorFactory(Properties defaultMessages, boolean autoDetectConstraints) {
        constraintValidatorInstancesMap = new HashMap<>();
        constraintMappings = new ArrayList<>();

        this.setDefaultMessages(defaultMessages)
            .setAutoDetectConstraints(autoDetectConstraints);
    }

    public Map<Class<?>, Object> getConstraintValidatorInstancesMap() {
        return constraintValidatorInstancesMap;
    }

    public List<ConstraintMapping<?>> getConstraintMappings() {
        return constraintMappings;
    }

    public Validator getValidator() {
        return validator == null ? validator = new Validator(this) : validator;
    }

    public boolean isAutoDetectConstraints() {
        return autoDetectConstraints;
    }

    public ValidatorFactory setAutoDetectConstraints(boolean autoDetectConstraints) {
        this.autoDetectConstraints = autoDetectConstraints;
        return this;
    }

    public Properties getDefaultMessages() {
        return defaultMessages;
    }

    public ValidatorFactory setDefaultMessages(Properties defaultMessages) {
        Assert.notNull(defaultMessages, "L'argument defaultMessages ne peut pas être \"null\"");

        this.defaultMessages = defaultMessages;
        return this;
    }

    public boolean hasDefaultMessage(String key) {
        Assert.notBlank(key, false, "L'argument key ne peut pas être vide ou \"null\"");

        return defaultMessages.containsKey(key);
    }

    @Nullable
    public String getDefaultMessage(String key) {
        Assert.notBlank(key, false, "L'argument key ne peut pas être vide ou \"null\"");

        return defaultMessages.getProperty(key);
    }

    public ValidatorFactory addDefaultMessages(@Nullable Properties properties) {
        if (properties == null) return this;

        defaultMessages.putAll(properties);
        return this;
    }

    public ValidatorFactory addDefaultMessage(String key, @Nullable String value) {
        Assert.notBlank(key, false, "L'argument key ne peut pas être vide ou \"null\"");

        defaultMessages.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> ConstraintMapping<A> getConstraintMapping(Class<A> annotationClass) {
        return (ConstraintMapping<A>) constraintMappings.stream()
            .filter(mapping -> mapping.getAnnotationClass().equals(annotationClass))
            .findFirst()
            .orElseThrow(InternalException::new);
    }

    public <A extends Annotation> ValidatorFactory addConstraintMapping(Class<A> annotationClass) {
        constraintMappings.add(new ConstraintMapping<>(annotationClass));
        return this;
    }

    public static ValidatorFactory buildDefault() {
        Properties defaultMessages = null;
        try (Resource resource = new ClassPathResource("default-validation-messages.properties")) {
            defaultMessages = new Properties();
            defaultMessages.load(resource.getInputStream());
        } catch (IOException ignored) { }

        return new ValidatorFactory(defaultMessages, true);
    }

    @SuppressWarnings("unchecked")
    public <T extends ConstraintValidator<?, ?>> T getConstraintValidatorInstance(Class<T> clazz) {
        Assert.notNull(clazz, "La classe de l'instance du contraintValidator à récupérer ne peut pas être \"null\"");

        return (T) constraintValidatorInstancesMap.computeIfAbsent(clazz, k -> {
            try {
                return k.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new ValidationProcessException(e);
            } catch (InvocationTargetException e) {
                throw new ValidationProcessException(e.getCause());
            }
        });
    }
}
