package mg.matsd.javaframework.validation.base;

import java.lang.annotation.Annotation;

public interface ConstraintValidator<A extends Annotation, T> {
    boolean isValid(T t);
}
