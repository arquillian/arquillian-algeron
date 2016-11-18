package org.arquillian.algeron.pact.provider.loader.maven;

import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves Maven artifacts.
 * This retriever gets artifacts from Maven repo, unpack them in a temporal directory and read the json contracts.
 * Notice that you can use http://maven.apache.org/enforcer/enforcer-rules/versionRanges.html and loader will get the highest version.
 */
public class ContractsMavenDependencyLoader implements ContractsRetriever {

    private final static int NAME = 0;
    private final static int URL = 1;
    private final static int LAYOUT = 2;

    private ContractsMavenDependency contractsMavenDependency;

    public ContractsMavenDependencyLoader(ContractsMavenDependency contractsMavenDependency) {
        this.contractsMavenDependency = contractsMavenDependency;
    }

    @Override
    public List<URI> retrieve() throws IOException {
        final File contractsFolder = createTemporaryFolder("MavenContracts");
        extractContracts(contractsFolder);
        return Arrays.stream(contractsFolder.listFiles())
                .map(file -> "file://" + file.getAbsolutePath())
                .map(URI::create)
                .collect(Collectors.toList());
    }

    private void extractContracts(File temporaryDirectory) throws IOException {
        final List<JavaArchive> resolvedContracts = resolveContracts();
        unpack(temporaryDirectory, resolvedContracts);
    }

    protected void unpack(File destination, List<JavaArchive> contracts) throws IOException {
        for (JavaArchive contract : contracts) {
            unpack(destination, contract);
        }
    }

    private void unpack(File destination, JavaArchive file) throws IOException {

        final Node rootDir = file.get("/");
        final Set<Node> contractFiles = rootDir.getChildren();

        for(Node contractFile : contractFiles) {
            final String filename = contractFile.getPath().get().substring(1);
            final Asset asset = contractFile.getAsset();
            try(final InputStream in = asset.openStream()) {
                Files.copy(in, new File(destination, filename).toPath());
            }
        }
    }

    protected List<JavaArchive> resolveContracts() {

        List<JavaArchive> contracts = new ArrayList<>();

        final ConfigurableMavenResolverSystem configurableMavenResolverSystem = Maven.configureResolver();

        if (!"".equals(contractsMavenDependency.customSettings())) {
            configurableMavenResolverSystem.fromClassloaderResource(RunnerExpressionParser.parseExpressions(contractsMavenDependency.customSettings()));
        }

        if (contractsMavenDependency.offline()) {
            configurableMavenResolverSystem.workOffline();
        }

        if (!"".equals(contractsMavenDependency.remoteRepository())) {
            final String[] remoteRepository = getRemoteRepository(RunnerExpressionParser.parseExpressions(contractsMavenDependency.remoteRepository()));
            configurableMavenResolverSystem.withRemoteRepo(remoteRepository[NAME],remoteRepository[URL], remoteRepository[LAYOUT]);
        }

        String[] coordinates = contractsMavenDependency.value();

        for (String coordinate : coordinates) {
            contracts.add(resolve(RunnerExpressionParser.parseExpressions(coordinate), configurableMavenResolverSystem));
        }

        return contracts;

    }

    private JavaArchive resolve(String coordinate, ConfigurableMavenResolverSystem maven) {
        final MavenCoordinate highestVersion = maven.resolveVersionRange(coordinate).getHighestVersion();
        return maven.resolve(highestVersion.toCanonicalForm()).withoutTransitivity().asSingle(JavaArchive.class);
    }

    private String[] getRemoteRepository(String remoteRepoDefinition) {
        final String[] elements = remoteRepoDefinition.split(":");
        if (elements.length != 3) {
            throw new IllegalArgumentException(String.format("Remote Repository must follow the syntax name:url:layout instead of %s", remoteRepoDefinition));
        }

        return elements;
    }

    private File createTemporaryFolder(String provider) throws IOException {
        File createdFolder = File.createTempFile("contracts", provider);
        createdFolder.delete();
        createdFolder.mkdir();
        return createdFolder;
    }

}
