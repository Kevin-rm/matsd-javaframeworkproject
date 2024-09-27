package mg.matsd.javaframework.orm.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(JoinColumns.class)
public @interface JoinColumn {
    String  name();

    String  referencedColumnName() default "";

    boolean nullable()             default true;

    boolean unique()               default false;

    boolean updatable()            default true;
}
