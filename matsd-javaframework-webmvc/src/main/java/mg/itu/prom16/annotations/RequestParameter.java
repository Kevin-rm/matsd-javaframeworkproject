package mg.itu.prom16.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestParameter {
    String  name()         default "";

    String  defaultValue() default "";

    boolean required()     default false;
}
