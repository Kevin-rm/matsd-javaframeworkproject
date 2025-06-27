package mg.matsd.javaframework.core.annotations.metadata;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CallOnce { }
