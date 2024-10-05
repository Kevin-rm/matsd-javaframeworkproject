package mg.itu.prom16.annotations;

import mg.itu.prom16.http.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMapping(methods = RequestMethod.POST)
public @interface Post {
    String value() default "";

    String name() default "";
}
