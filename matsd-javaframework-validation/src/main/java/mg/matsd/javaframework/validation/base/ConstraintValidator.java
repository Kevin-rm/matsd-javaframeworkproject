package mg.matsd.javaframework.validation.base;

import java.lang.annotation.Annotation;

public interface ConstraintValidator<A extends Annotation, T> {

    void initialize(A constraintAnnotation);

    boolean isValid(T t);
}
