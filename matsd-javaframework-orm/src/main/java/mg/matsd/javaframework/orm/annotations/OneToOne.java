package mg.matsd.javaframework.orm.annotations;

import mg.matsd.javaframework.orm.mapping.FetchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {
    Class<?>  targetEntity()  default void.class;

    String    mappedBy()      default "";

    boolean   optional()      default true;

    boolean   orphanRemoval() default false;

    FetchType fetchType()     default FetchType.LAZY;
}
