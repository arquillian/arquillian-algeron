package org.arquillian.algeron.pact.consumer.core.util;

import org.arquillian.algeron.pact.consumer.spi.Pact;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolveClassAnnotationTest {

    @Test
    public void should_get_annotation_from_class_level() {
        assertThat(ResolveClassAnnotation.getClassWithAnnotation(A.class, Pact.class)).isPresent();
    }

    @Test
    public void should_get_annotation_from_subclass() {
        assertThat(ResolveClassAnnotation.getClassWithAnnotation(B.class, Pact.class)).isPresent();
    }

    @Pact(consumer = "c", provider = "p")
    public static class A {
    }

    public static class B extends A {
    }
}
