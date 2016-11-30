package org.arquillian.algeron.pact.provider.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentPatternMatcher {

    public ArgumentPatternMatcher() {
    }

    public static List<String> arguments(Pattern pattern, String state) {
        final List<String> argumentsValues = new ArrayList<>();
        Matcher matcher = pattern.matcher(state);
        if (matcher.lookingAt()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                int startIndex = matcher.start(i);

                if (startIndex == -1) {
                    argumentsValues.add("");
                } else {
                    argumentsValues.add(matcher.group(i));
                }
            }
        }

        return argumentsValues;

    }

}
