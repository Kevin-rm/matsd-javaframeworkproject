package mg.itu.prom16.annotations;

import mg.itu.prom16.http.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {
    String value()            default "";

    String name()             default "";

    RequestMethod[] methods() default {};
}
