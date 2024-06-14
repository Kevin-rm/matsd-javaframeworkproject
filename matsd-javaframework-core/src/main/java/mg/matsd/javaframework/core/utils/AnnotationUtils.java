package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

public final class AnnotationUtils {
    private AnnotationUtils() { }

    public static Set<Annotation> getAllAnnotations(AnnotatedElement annotatedElement) {
        Assert.notNull(annotatedElement, "L'argument annotatedElement ne peut pas être \"null\"");

        Set<Annotation> annotations = new HashSet<>();
        doCollectAnnotations(annotatedElement, annotations);

        return annotations;
    }

    @Nullable
    public static Annotation getAnnotation(Class<? extends Annotation> annotationClass, AnnotatedElement annotatedElement) {
        Assert.notNull(annotationClass, "La classe de l'annotation ne peut pas être \"null\"");

        Set<Annotation> annotations = getAllAnnotations(annotatedElement);
        for (Annotation annotation : annotations)
            if (annotation.annotationType().equals(annotationClass))
                return annotation;

        return null;
    }

    public static boolean hasAnnotation(Class<? extends Annotation> annotationClass, AnnotatedElement annotatedElement) {
        return getAnnotation(annotationClass, annotatedElement) != null;
    }

    private static void doCollectAnnotations(AnnotatedElement annotatedElement, Set<Annotation> annotations) {
        for (Annotation annotation : annotatedElement.getAnnotations())
            if (annotations.add(annotation))
                doCollectAnnotations(annotation.annotationType(), annotations);
    }
}
