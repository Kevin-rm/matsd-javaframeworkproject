package mg.itu.prom16.annotations;

import mg.matsd.javaframework.di.annotations.Component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface Controller { }
