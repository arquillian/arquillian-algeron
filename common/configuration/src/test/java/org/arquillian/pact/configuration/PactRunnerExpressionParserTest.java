package org.arquillian.pact.configuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PactRunnerExpressionParserTest {

    @Test
    public void should_return_value_if_no_placeholders() {
        assertThat(PactRunnerExpressionParser.parseExpressions("myvalue")).isEqualTo("myvalue");
    }

    @Test
    public void should_return_value_if_from_sys_prop_if_placeholders_used() {
        try {
            System.setProperty("myprop", "myvalue");
            assertThat(PactRunnerExpressionParser.parseExpressions("${myprop}")).isEqualTo("myvalue");
        } finally {
            System.clearProperty("myprop");
        }
    }

    @Test
    public void should_return_default_value_if_no_sys_prop() {
        assertThat(PactRunnerExpressionParser.parseExpressions("${myprop2:myvalue}")).isEqualTo("myvalue");
    }

    @Test
    public void colon_are_not_for_defaults() {
        try {
            System.setProperty("myprop-colon", "myvalue");
            assertThat(PactRunnerExpressionParser.parseExpressions("${myprop-colon}")).isEqualTo("myvalue");
        } finally {
            System.clearProperty("myprop-colon");
        }
    }

    @Test
    public void can_use_default_chars_if_not_sys_prop() {
        assertThat(PactRunnerExpressionParser.parseExpressions("myvalue:secondvalue")).isEqualTo("myvalue:secondvalue");
    }

    @Test
    public void should_return_empty_string_if_default_value_separator_is_present_but_no_default_value() {
        assertThat(PactRunnerExpressionParser.parseExpressions("${myprop2:}")).isEqualTo("");
    }

    @Test
    public void should_scan_only_first_colon_as_property() {
        assertThat(PactRunnerExpressionParser.parseExpressions("${giturl:http://localhost:3000/alex/gamer-contracts.git}")).isEqualTo("http://localhost:3000/alex/gamer-contracts.git");
    }


}
