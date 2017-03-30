package org.arquillian.algeron.pact.provider.core;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class ArrayMatcher {

    public static final boolean matches(String[] patternArray, String value) {
        return firstMatch(patternArray, value).isPresent();
    }

    public static final Optional<String> firstMatch(String[] patternArray, String value) {
        return Arrays.stream(patternArray)
            .filter(pattern -> Pattern.compile(pattern).matcher(value).lookingAt())
            .findFirst();
    }
}
