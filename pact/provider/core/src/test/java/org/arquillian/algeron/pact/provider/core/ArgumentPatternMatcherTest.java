package org.arquillian.algeron.pact.provider.core;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgumentPatternMatcherTest {

    @Test
    public void should_extract_arguments_if_number() {
        List<String> arguments = ArgumentPatternMatcher.arguments(Pattern.compile("I have (\\d+) cukes in my belly"), "I have 36 cukes in my belly");
        assertThat(arguments).containsExactly("36");
    }

    @Test
    public void should_extract_arguments_if_list() {
        List<String> arguments = ArgumentPatternMatcher.arguments(Pattern.compile("The following animals: (.*)"), "The following animals: dog, cat, mouse");
        assertThat(arguments).containsExactly("dog, cat, mouse");
    }

    @Test
    public void should_extract_multiple_arguments() {
        List<String> arguments = ArgumentPatternMatcher.arguments(Pattern.compile("I have (\\d+) cukes in my (\\w+)"), "I have 36 cukes in my belly");
        assertThat(arguments).containsExactly("36", "belly");
    }

    @Test
    public void should_not_extract_anything_if_no_matches() {
        List<String> arguments = ArgumentPatternMatcher.arguments(Pattern.compile("The following animals: (.*)"), "I have 36 cukes in my belly");
        assertThat(arguments).isEmpty();
    }

}
