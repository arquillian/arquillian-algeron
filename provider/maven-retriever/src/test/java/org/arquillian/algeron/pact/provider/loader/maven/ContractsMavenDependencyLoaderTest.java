package org.arquillian.algeron.pact.provider.loader.maven;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ContractsMavenDependencyLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_unpack_pacts() throws IOException {

        final JavaArchive contract = ShrinkWrap.create(JavaArchive.class, "contract.jar")
            .add(new StringAsset("My contract"), "/contract.json");

        ContractsMavenDependencyLoader contractsMavenDependencyLoader =
            new ContractsMavenDependencyLoader(MavenLoaderTest.class.getAnnotation(ContractsMavenDependency.class));

        contractsMavenDependencyLoader.unpack(folder.getRoot(), Arrays.asList(contract));
        final File contractFile = new File(folder.getRoot(), "contract.json");
        assertThat(contractFile).exists();
        assertThat(contractFile).hasContent("My contract");
    }

    @ContractsMavenDependency(value = "org.superbiz:contract:[1.0,]")
    private static class MavenLoaderTest {
    }
}
