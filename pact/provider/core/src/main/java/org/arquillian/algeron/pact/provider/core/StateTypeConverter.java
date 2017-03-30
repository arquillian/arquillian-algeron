package org.arquillian.algeron.pact.provider.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class StateTypeConverter {

    /**
     * A helper converting method.
     * <p>
     * Converts string to a class of given type
     *
     * @param value
     *     String value to be converted
     * @param to
     *     Type of desired value
     * @param <T>
     *     Type of returned value
     *
     * @return Value converted to the appropriate type
     */
    public static <T> T convert(String value, Class<T> to) {
        if (value == null && !(String.class.equals(to) || String[].class.equals(to))) {
            return null;
        }

        if (String.class.equals(to)) {
            return to.cast(value);
        } else if (Integer.class.equals(to)) {
            return to.cast(Integer.valueOf(value));
        } else if (int.class.equals(to)) {
            return (T) Integer.valueOf(value);
        } else if (Double.class.equals(to)) {
            return to.cast(Double.valueOf(value));
        } else if (double.class.equals(to)) {
            return (T) Double.valueOf(value);
        } else if (Long.class.equals(to)) {
            return to.cast(Long.valueOf(value));
        } else if (long.class.equals(value)) {
            return (T) Long.valueOf(value);
        } else if (Boolean.class.equals(to)) {
            return to.cast(Boolean.valueOf(value));
        } else if (boolean.class.equals(to)) {
            return (T) Boolean.valueOf(value);
        } else if (URL.class.equals(to)) {
            try {
                return to.cast(new URI(value).toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            }
        } else if (URI.class.equals(to)) {
            try {
                return to.cast(new URI(value));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            }
        } else {
            String trimmedValue = extractEnumName(value);
            if (to.isEnum()) {
                return (T) Enum.valueOf((Class<Enum>) to, trimmedValue.toUpperCase());
            } else if (String[].class.equals(to)) {
                final String[] convertedArray = value.split(",");
                if (convertedArray.length == 0) {
                    return to.cast(new String[0]);
                }

                trimElements(convertedArray);

                if (convertedArray.length == 1 && hasOnlyBlanks(convertedArray)) {
                    return to.cast(new String[0]);
                }

                return to.cast(convertedArray);
            } else if (Collection.class.isAssignableFrom(to)) {
                final String[] convertedArray = value.split(",");
                if (convertedArray.length == 0) {
                    return to.cast(new ArrayList<>());
                }

                trimElements(convertedArray);

                if (convertedArray.length == 1 && hasOnlyBlanks(convertedArray)) {
                    return to.cast(new ArrayList<>());
                }

                return to.cast(Arrays.asList(convertedArray));
            } else if (Charset.class.equals(to)) {
                return to.cast(Charset.forName(trimmedValue.toUpperCase()));
            } else if (Class.class.equals(to)) {
                try {
                    Object clazz = Class.forName(value);
                    return to.cast(clazz);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to find class [" + value + "].", e);
                }
            } else // Try to create instance via reflection
            {
                try {
                    Object instance = Class.forName(value).newInstance();
                    return to.cast(instance);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Unable to convert value [" + value + "] to a class [" + to.getName() + "].", e);
                }
            }
        }
    }

    private static String extractEnumName(final String value) {
        String trimmedValue = value.trim();
        final int lastDot = trimmedValue.lastIndexOf('.');
        final boolean potentiallyFullyQualifiedEnumName = lastDot > 0 && trimmedValue.length() > 1;
        if (potentiallyFullyQualifiedEnumName) {
            trimmedValue = trimmedValue.substring(lastDot + 1);
        }
        return trimmedValue;
    }

    private static void trimElements(String[] convertedArray) {
        if (convertedArray == null) {
            return;
        }

        for (int i = 0; i < convertedArray.length; i++) {
            convertedArray[i] = convertedArray[i].trim();
        }
    }

    private static boolean hasOnlyBlanks(String[] convertedArray) {
        boolean hasOnlyBlanks = true;
        for (String element : convertedArray) {
            if (isBlank(element)) {
                return false;
            }
        }
        return hasOnlyBlanks;
    }

    private static boolean isBlank(String element) {
        return element.trim().length() != 0;
    }
}
