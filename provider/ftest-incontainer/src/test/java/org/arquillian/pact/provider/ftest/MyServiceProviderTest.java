package org.arquillian.pact.provider.ftest;

import org.arquillian.pact.provider.core.httptarget.Target;
import org.arquillian.pact.provider.core.loader.PactFolder;
import org.arquillian.pact.provider.spi.Provider;
import org.arquillian.pact.provider.spi.State;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Provider("test_provider")
@PactFolder("pacts")
public class MyServiceProviderTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClass(MyService.class);
    }

    @ArquillianResource
    URL webapp;

    @ArquillianResource
    Target target;

    @State("test state")
    public void testStateMethod(Map<String, Object> params) {
        assertThat(params).containsEntry("name", "Alexandra");
    }

    @Test
    public void should_provide_valid_answers() {
        target.testInteraction(webapp);
    }

}
