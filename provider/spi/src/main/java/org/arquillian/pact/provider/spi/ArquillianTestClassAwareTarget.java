package org.arquillian.pact.provider.spi;

import org.jboss.arquillian.test.spi.TestClass;

public interface ArquillianTestClassAwareTarget {

    void setTestClass(TestClass testClass, Object testInstance);

}
