package org.arquillian.algeron.pact.provider.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayMatcherTest {

    @Test
    public void should_match_any_possible_pattern() {
        String[] states = new String[]{"I have (\\d+) cukes in my belly", "The following animals: (.*)"};
        assertThat(ArrayMatcher.matches(states, "I have 36 cukes in my belly")).isTrue();
    }

    @Test
    public void should_match_with_no_patterns() {
        String[] states = new String[]{"I have 36 cukes in my belly"};
        assertThat(ArrayMatcher.matches(states, "I have 36 cukes in my belly")).isTrue();
    }

    @Test
    public void should_not_match_if_not_equals() {
        String[] states = new String[]{"The following animals: (.*)"};
        assertThat(ArrayMatcher.matches(states, "I have 36 cukes in my belly")).isFalse();
    }

}
