package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AnnotationUtils {

    public static Set<Annotation> getAllAnnotations(AnnotatedElement annotatedElement) {
        Assert.notNull(annotatedElement, "L'argument annotatedElement ne peut pas être \"null\"");

        Set<Annotation> annotations = new HashSet<>();
        doCollectAnnotations(annotatedElement, annotations);

        return annotations;
    }

    @Nullable
    public static Annotation getAnnotation(Class<? extends Annotation> annotationClass, AnnotatedElement annotatedElement) {
        Assert.notNull(annotationClass, "La classe de l'annotation ne peut pas être \"null\"");

        Annotation annotation = annotatedElement.getAnnotation(annotationClass);
        if (annotation != null) return annotation;

        return getAllAnnotations(annotatedElement).stream()
            .filter(a -> a.annotationType().equals(annotationClass))
            .findFirst()
            .orElse(null);
    }

    public static boolean hasAnnotation(Class<? extends Annotation> annotationClass, AnnotatedElement annotatedElement) {
        return getAnnotation(annotationClass, annotatedElement) != null;
    }

    private static void doCollectAnnotations(AnnotatedElement annotatedElement, Set<Annotation> annotations) {
        Arrays.stream(annotatedElement.getAnnotations())
            .filter(annotations::add)
            .forEachOrdered(annotation -> doCollectAnnotations(annotation.annotationType(), annotations));
    }
}
