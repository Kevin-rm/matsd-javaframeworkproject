package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.util.Properties;

public class ValidatorFactory {
    public static final String MATSD_VALIDATION_MESSAGE_PREFIX = "mg.matsd.javaframework.validation.constraints";
    private static ValidatorFactory instance;

    private final ConstraintValidatorFactory constraintValidatorFactory;
    private Validator validator;
    private Properties defaultMessages;

    private ValidatorFactory() {
        constraintValidatorFactory = new ConstraintValidatorFactory();

        try (Resource resource = new ClassPathResource("default-validation-messages.properties")) {
            defaultMessages = new Properties();
            defaultMessages.load(resource.getInputStream());
        } catch (IOException ignored) { }
    }

    public static ValidatorFactory getInstance() {
        return instance == null ? instance = new ValidatorFactory() : instance;
    }

    public synchronized Validator getValidator() {
        return validator == null ? validator = new Validator(this) : validator;
    }

    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return constraintValidatorFactory;
    }

    public Properties getDefaultMessages() {
        return defaultMessages;
    }

    public ValidatorFactory setDefaultMessages(Properties defaultMessages) {
        Assert.notNull(defaultMessages, "L'argument defaultMessages ne peut pas être \"null\"");

        this.defaultMessages = defaultMessages;
        return this;
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
}
