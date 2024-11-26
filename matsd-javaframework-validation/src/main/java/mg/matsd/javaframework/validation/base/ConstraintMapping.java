package mg.matsd.javaframework.validation.base;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.annotations.Constraint;
import mg.matsd.javaframework.validation.exceptions.ConstraintDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ConstraintMapping<A extends Annotation> {
    private Class<A> annotationClass;
    private Class<? extends ConstraintValidator<A, ?>>[] constraintValidatorClasses;
    private Method messageMethod;
    private Method groupsMethod;

    ConstraintMapping(Class<A> annotationClass) {
        this.setAnnotationClass(annotationClass)
            .setConstraintValidatorClasses()
            .setMessageMethod()
            .setGroupsMethod();
    }

    public Class<A> getAnnotationClass() {
        return annotationClass;
    }

    private ConstraintMapping<A> setAnnotationClass(Class<A> annotationClass) {
        System.out.println(annotationClass);
        Assert.notNull(annotationClass, "L'argument annotationClass ne peut pas être \"null\"");
        Assert.state(annotationClass.isAnnotationPresent(Constraint.class),
            "Une annotation de contrainte doit être annotée avec \"@Constraint\"");

        this.annotationClass = annotationClass;
        return this;
    }

    public Class<? extends ConstraintValidator<A, ?>>[] getConstraintValidatorClasses() {
        return constraintValidatorClasses;
    }

    @SuppressWarnings("unchecked")
    private ConstraintMapping<A> setConstraintValidatorClasses() {
        constraintValidatorClasses = (Class<? extends ConstraintValidator<A, ?>>[]) annotationClass.getAnnotation(Constraint.class)
            .validatedBy();
        return this;
    }

    public Method getMessageMethod() {
        return messageMethod;
    }

    private ConstraintMapping<A> setMessageMethod() {
        try {
            messageMethod = annotationClass.getMethod("message");
        } catch (NoSuchMethodException e) {
            throw new ConstraintDefinitionException(String.format("Aucune méthode nommée \"message\" " +
                "trouvée sur l'annotation : \"%s\"", annotationClass.getName()));
        }

        return this;
    }

    public Method getGroupsMethod() {
        return groupsMethod;
    }

    private ConstraintMapping<A> setGroupsMethod() {
        try {
            groupsMethod = annotationClass.getMethod("groups");
        } catch (NoSuchMethodException e) {
            throw new ConstraintDefinitionException(String.format("Aucune méthode nommée \"groups\" " +
                "trouvée sur l'annotation : \"%s\"", annotationClass.getName()));
        }

        return this;
    }

    @Override
    public String toString() {
        return "ConstraintMapping{" +
            "annotationClass=" + annotationClass +
            ", constraintValidatorClasses=" + Arrays.toString(constraintValidatorClasses) +
            '}';
    }
}
