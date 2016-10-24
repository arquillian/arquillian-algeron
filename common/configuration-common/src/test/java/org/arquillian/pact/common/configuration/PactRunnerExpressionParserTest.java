package org.arquillian.pact.common.configuration;

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

}
