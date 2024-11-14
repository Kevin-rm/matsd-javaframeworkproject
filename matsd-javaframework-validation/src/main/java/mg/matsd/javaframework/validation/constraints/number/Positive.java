package mg.matsd.javaframework.validation.constraints.number;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Positive {
    String message()    default "{{ mg.matsd.javaframework.validation.constraints.number.positive.message }}";

    Class<?>[] groups() default {};
}
