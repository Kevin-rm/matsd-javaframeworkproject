package mg.matsd.javaframework.orm.annotations;

import mg.matsd.javaframework.orm.mapping.SQLTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String   name();

    boolean  nullable()         default true;

    boolean  unique()           default false;

    SQLTypes columnDefinition() default SQLTypes.VARCHAR;

    int      length()           default 255;

    int      precision()        default 18;

    int      scale()            default 0;

    boolean  updatable()        default true;
}
