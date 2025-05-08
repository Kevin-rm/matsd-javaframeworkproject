package mg.itu.prom16.annotations;

import mg.matsd.javaframework.servletwrapper.http.RequestMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@RequestMapping(methods = RequestMethod.POST)
public @interface Post {
    String value() default "";

    String name() default "";
}
