package mg.matsd.javaframework.core.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD
})
@Documented
public @interface Nullable { }
