package mg.itu.prom16.annotations;

import mg.itu.prom16.http.RequestMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface RequestMapping {
    String value()            default "";

    String name()             default "";

    RequestMethod[] methods() default {};
}
