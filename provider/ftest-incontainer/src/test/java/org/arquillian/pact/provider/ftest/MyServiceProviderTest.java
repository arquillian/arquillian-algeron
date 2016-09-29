package org.arquillian.pact.provider.ftest;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.RequestResponseInteraction;
import org.arquillian.pact.provider.core.httptarget.Target;
import org.arquillian.pact.provider.core.loader.PactFolder;
import org.arquillian.pact.provider.spi.CurrentConsumer;
import org.arquillian.pact.provider.spi.CurrentInteraction;
import org.arquillian.pact.provider.spi.Provider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

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

    @Test
    public void should_provide_valid_answers() {
        target.testInteraction(webapp);
    }

}
