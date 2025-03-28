package mg.matsd.javaframework.validation.annotations;

import mg.matsd.javaframework.validation.base.ConstraintValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Constraint {
    Class<? extends ConstraintValidator<?, ?>>[] validatedBy();
}
