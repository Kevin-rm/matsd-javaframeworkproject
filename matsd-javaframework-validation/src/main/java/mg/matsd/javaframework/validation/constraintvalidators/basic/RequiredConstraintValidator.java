package mg.matsd.javaframework.validation.constraintvalidators.basic;

import mg.matsd.javaframework.validation.constraints.basic.Required;
import mg.matsd.javaframework.validation.base.ConstraintValidator;

public class RequiredConstraintValidator implements ConstraintValidator<Required, Object> {

    @Override
    public void initialize(Required constraintAnnotation) { }

    @Override
    public boolean isValid(Object value) {
        if (value == null) return false;
        if (value instanceof String string) return !string.isBlank();

        return true;
    }
}
