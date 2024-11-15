package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.util.Properties;

public class ValidatorFactory {
    private static final Properties DEFAULT_MESSAGES = new Properties();
    private static Validator validator;

    static {
        try (Resource resource = new ClassPathResource("default-validation-messages.properties")) {
            DEFAULT_MESSAGES.load(resource.getInputStream());
        } catch (IOException ignored) { }
    }

    private ValidatorFactory() { }

    public static Validator getValidatorInstance() {
        return validator == null ? validator = new Validator() : validator;
    }

    @Nullable
    public static String getMessage(String key) {
        Assert.notBlank(key, false, "L'argument key ne peut pas être vide ou \"null\"");

        return DEFAULT_MESSAGES.getProperty(key);
    }
}
