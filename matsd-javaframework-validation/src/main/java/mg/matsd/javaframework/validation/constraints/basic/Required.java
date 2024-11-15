package mg.matsd.javaframework.validation.constraints.basic;

import mg.matsd.javaframework.validation.base.Constraint;
import mg.matsd.javaframework.validation.constraintvalidators.basic.RequiredConstraintValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = { RequiredConstraintValidator.class })
public @interface Required {
    String message()    default "{{ mg.matsd.javaframework.validation.constraints.basic.required.message }}";

    Class<?>[] groups() default {};
}
