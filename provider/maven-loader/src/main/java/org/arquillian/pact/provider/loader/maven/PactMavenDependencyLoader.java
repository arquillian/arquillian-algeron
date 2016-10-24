package org.arquillian.pact.provider.loader.maven;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactReader;
import org.arquillian.pact.common.configuration.PactRunnerExpressionParser;
import org.arquillian.pact.provider.spi.loader.PactLoader;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves Maven artifacts.
 * This loader gets artifacts from Maven repo, unpack them in a temporal directory and read the json contracts.
 * Notice that you can use http://maven.apache.org/enforcer/enforcer-rules/versionRanges.html and loader will get the highest version.
 */
public class PactMavenDependencyLoader implements PactLoader {

    private final static int NAME = 0;
    private final static int URL = 1;
    private final static int LAYOUT = 2;

    private PactMavenDependency pactMavenDependency;

    public PactMavenDependencyLoader(PactMavenDependency pactMavenDependency) {
        this.pactMavenDependency = pactMavenDependency;
    }

    @Override
    public List<Pact> load(String providerName) throws IOException {

        final File pactFolder = createTemporaryFolder(providerName);
        extractContracts(pactFolder);
        return loadPacts(providerName, pactFolder);
    }

    protected List<Pact> loadPacts(String providerName, File pactFolder) {
        File[] files = pactFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            return Arrays.stream(files)
                    .map(PactReader::loadPact)
                    .filter( pact -> pact.getProvider().getName().equals(providerName))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void extractContracts(File temporaryDirectory) throws IOException {
        final List<JavaArchive> resolvedPacts = resolvePacts();
        unpack(temporaryDirectory, resolvedPacts);
    }

    protected void unpack(File destination, List<JavaArchive> pacts) throws IOException {
        for (JavaArchive pact : pacts) {
            unpack(destination, pact);
        }
    }

    private void unpack(File destination, JavaArchive file) throws IOException {

        final Node rootDir = file.get("/");
        final Set<Node> pactFiles = rootDir.getChildren();

        for(Node pactFile : pactFiles) {
            final String filename = pactFile.getPath().get().substring(1);
            final Asset asset = pactFile.getAsset();
            try(final InputStream in = asset.openStream()) {
                Files.copy(in, new File(destination, filename).toPath());
            }
        }
    }

    protected List<JavaArchive> resolvePacts() {

        List<JavaArchive> pacts = new ArrayList<>();

        final ConfigurableMavenResolverSystem configurableMavenResolverSystem = Maven.configureResolver();

        if (!"".equals(pactMavenDependency.customSettings())) {
            configurableMavenResolverSystem.fromClassloaderResource(PactRunnerExpressionParser.parseExpressions(pactMavenDependency.customSettings()));
        }

        if (pactMavenDependency.offline()) {
            configurableMavenResolverSystem.workOffline();
        }

        if (!"".equals(pactMavenDependency.remoteRepository())) {
            final String[] remoteRepository = getRemoteRepository(PactRunnerExpressionParser.parseExpressions(pactMavenDependency.remoteRepository()));
            configurableMavenResolverSystem.withRemoteRepo(remoteRepository[NAME],remoteRepository[URL], remoteRepository[LAYOUT]);
        }

        String[] coordinates = pactMavenDependency.value();

        for (String coordinate : coordinates) {
            pacts.add(resolve(PactRunnerExpressionParser.parseExpressions(coordinate), configurableMavenResolverSystem));
        }

        return pacts;

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
