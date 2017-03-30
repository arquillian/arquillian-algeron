package org.arquillian.algeron.pact.consumer.core.util;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Class that returns if a class or any subclass is annotated with given annotation.
 */
public class ResolveClassAnnotation {

    /**
     * Class that returns if a class or any subclass is annotated with given annotation.
     *
     * @param source          class.
     * @param annotationClass to find.
     * @return Class containing the annotation.
     */
    public static Optional<Class<?>> getClassWithAnnotation(final Class<?> source, final Class<? extends Annotation> annotationClass) {

        Class<?> nextSource = source;
        while (nextSource != Object.class) {
            if (nextSource.isAnnotationPresent(annotationClass)) {
                return Optional.of(nextSource);
            } else {
                nextSource = nextSource.getSuperclass();
            }
        }

        return Optional.empty();
    }

}
