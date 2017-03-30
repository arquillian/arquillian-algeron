package org.arquillian.algeron.pact.provider.core;

import net.jcip.annotations.NotThreadSafe;
import org.assertj.core.util.Arrays;
import org.junit.Test;

import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class StateTypeConverterTest {

    @Test
    public void should_convert_empty_string_to_empty_string_array() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        String[] convertedStringArray = typeConverter.convert("", String[].class);

        // then
        assertThat(convertedStringArray).isEmpty();
    }

    @Test
    public void should_convert_blank_string_to_empty_string_array() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        String[] convertedStringArray = typeConverter.convert("        ", String[].class);

        // then
        assertThat(convertedStringArray).isEmpty();
    }

    @Test
    public void should_convert_sequence_of_blank_strings_to_empty_string_array() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();
        String[] arrayOfEmptyStrings = Arrays.array("", "", "", "", "");

        // when
        String[] convertedStringArray = typeConverter.convert(" ,   ,   ,   ,       ", String[].class);

        // then
        assertThat(convertedStringArray).isEqualTo(arrayOfEmptyStrings);
    }

    @Test
    public void should_convert_single_element_to_one_element_array() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();
        String[] singleElementArray = Arrays.array("one element");

        // when
        String[] convertedStringArray = typeConverter.convert("one element", String[].class);

        // then
        assertThat(convertedStringArray).isEqualTo(singleElementArray);
    }

    @Test
    public void should_convert_single_element_with_delimiter_to_one_element_array() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();
        String[] singleElementArray = Arrays.array("one element");

        // when
        String[] convertedStringArray = typeConverter.convert("one element,", String[].class);

        // then
        assertThat(convertedStringArray).isEqualTo(singleElementArray);
    }

    @Test
    public void should_convert_single_element_with_delimiters_to_one_element_array() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();
        String[] singleElementArray = Arrays.array("one element");

        // when
        String[] convertedStringArray = typeConverter.convert("one element,,,,,,,", String[].class);

        // then
        assertThat(convertedStringArray).isEqualTo(singleElementArray);
    }

    @Test
    public void should_convert_blank_to_empty_string_when_appear_in_sequence_with_non_blanks() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();
        String[] expectedArray = Arrays.array("a", "", "test", "", "", "b");
        // when
        String[] convertedStringArray = typeConverter.convert("a,   , test  ,   ,      , b ", String[].class);

        // then
        assertThat(convertedStringArray).isEqualTo(expectedArray);
    }

    @Test
    public void should_convert_string() throws Exception {
        // given
        String expectedString = "Hello";
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        String convertedString = typeConverter.convert("Hello", String.class);

        // then
        assertThat(convertedString).isEqualTo(expectedString);
    }

    @Test
    public void should_convert_string_to_integer() throws Exception {
        // given
        Integer expectedInteger = Integer.valueOf(15);
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        Integer convertedInteger = typeConverter.convert("15", Integer.class);

        // then
        assertThat(convertedInteger).isEqualTo(expectedInteger);
    }

    @Test
    public void should_convert_string_to_double() throws Exception {
        // given
        Double expecteDouble = Double.valueOf("123");
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        Double convertedDouble = typeConverter.convert("123", Double.class);

        // then
        assertThat(convertedDouble).isEqualTo(expecteDouble);
    }

    @Test
    public void should_convert_string_to_long() throws Exception {
        // given
        Long expectedLong = Long.valueOf(-456);
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        Long convertedLong = typeConverter.convert("-456", Long.class);

        // then
        assertThat(convertedLong).isEqualTo(expectedLong);
    }

    @Test
    public void should_convert_string_to_boolean() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        Boolean convertedBoolen = typeConverter.convert("True", Boolean.class);

        // then
        assertThat(convertedBoolen).isTrue();
    }

    @Test
    public void should_convert_string_to_URL() throws Exception {
        // given
        URL expectedUrl = new URI("http://www.arquillian.org").toURL();
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        URL convertedUrl = typeConverter.convert("http://www.arquillian.org", URL.class);

        // then
        assertThat(convertedUrl).isEqualTo(expectedUrl);
    }

    @Test
    public void should_convert_string_to_URI() throws Exception {
        // given
        URI expectedUri = new URI("http://www.arquillian.org");
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        URI convertedUri = typeConverter.convert("http://www.arquillian.org", URI.class);

        // then
        assertThat(convertedUri).isEqualTo(expectedUri);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_for_unsupported_type() throws Exception {
        // given
        StateTypeConverter typeConverter = new StateTypeConverter();

        // when
        typeConverter.convert("typeConverter", StateTypeConverter.class);

        // then
        // exception should be thrown
    }

    // -------------------------------------------------------------------------------------------
}
