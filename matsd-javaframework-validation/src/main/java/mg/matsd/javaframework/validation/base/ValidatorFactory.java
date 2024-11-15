package mg.matsd.javaframework.validation.base;

public class ValidatorFactory {
    private static Validator validator;

    private ValidatorFactory() { }

    public static Validator getValidatorInstance() {
        return validator == null ? validator = new Validator() : validator;
    }
}
