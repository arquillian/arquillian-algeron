package org.arquillian.algeron.pact.provider.spi;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.test.spi.TestClass;

public interface ArquillianTestClassAwareTarget {

    void setTestClass(TestClass testClass, Object testInstance);

    void setInjector(Injector injector);
}
