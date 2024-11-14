package mg.matsd.javaframework.validation.base.internal.constraintvalidators.number;

import mg.matsd.javaframework.validation.constraints.number.Positive;
import mg.matsd.javaframework.validation.base.ConstraintValidator;

public class PositiveConstraintValidator implements ConstraintValidator<Positive, Number> {

    @Override
    public void initialize(Positive constraintAnnotation) { }

    @Override
    public boolean isValid(Number value) {
        if (value == null) return false;

        if (value instanceof Integer) return value.intValue()    > 0;
        if (value instanceof Long)    return value.longValue()   > 0;
        if (value instanceof Double)  return value.doubleValue() > 0;
        if (value instanceof Float)   return value.floatValue()  > 0;
        if (value instanceof Short)   return value.shortValue()  > 0;
        if (value instanceof Byte)    return value.byteValue()   > 0;

        return false;
    }
}
