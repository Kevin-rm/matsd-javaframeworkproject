package mg.matsd.javaframework.security.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Authorize {
    String[] value() default {"IS_AUTHENTICATED"};
}
