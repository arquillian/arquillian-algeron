package org.arquillian.pact.provider.loader.git;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactReader;
import org.arquillian.pact.provider.api.PactRunnerExpressionParser;
import org.arquillian.pact.provider.spi.loader.PactLoader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Loader to get pact files from git repository.
 */
public class PactGitLoader implements PactLoader {

    private final static Logger logger = Logger.getLogger(PactGitLoader.class.getName());

    private PactGit pactGit;

    GitOperations gitOperations;

    public PactGitLoader(PactGit pactGit) {
        this.pactGit = pactGit;
        this.gitOperations = new GitOperations();
    }

    @Override
    public List<Pact> load(String providerName) throws IOException {

        Path location = getPactsFolderFromGitRepo();

        if (location != null) {
            File[] files = location.toFile().listFiles((dir, name) -> name.endsWith(".json"));

            if (files != null) {
                return Arrays.stream(files)
                        .map(PactReader::loadPact)
                        .filter(pact -> pact.getProvider().getName().equals(providerName))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    protected Path getPactsFolderFromGitRepo() throws IOException {
        Path location = null;
        Git git = null;
        try {
            if (isSet(this.pactGit.repository())) {

                Path repository = Paths.get(getResolvedValue(this.pactGit.repository()));

                if (this.gitOperations.isValidGitRepository(repository)) {

                    git = this.gitOperations.openGitRepository(repository);
                    if (this.gitOperations.hasAtLeastOneReference(git.getRepository())) {

                        final PullResult pullResult = executePull(git);

                        if (pullResult.isSuccessful()) {
                            location = moveToCorrectLocation(git);
                        } else {
                            // Merge conflicts
                            throw new IllegalArgumentException("There are merge conflicts into an existing git repo. Provider should not deal with merge conflicts. Correct them or delete the repo and execute again the test.");
                        }
                    } else {
                        throw new IllegalArgumentException(String.format("Git repository %s was not cloned correctly.", git.getRepository().getDirectory().getAbsolutePath()));
                    }
                } else {
                    logger.log(Level.INFO, String.format("%s directory is not a git directory or does not exists and it is going to be deleted and cloned", repository));

                    Files.deleteIfExists(repository);
                    Files.createDirectories(repository);
                    git = executeClone(repository);
                    location = moveToCorrectLocation(git);
                }

            } else {
                // Put files in a temp directory
                final Path testGitRepository = Files.createTempDirectory("TestGitRepository");

                logger.info(String.format("Repository is going to be cloned at %s", testGitRepository));

                git = executeClone(testGitRepository);
                location = moveToCorrectLocation(git);
            }

        } finally {
            if (git != null) {
                git.close();
            }
        }
        return location;
    }


    private Path moveToCorrectLocation(Git git) {
        //getRepository().getDirectory() returns the .git directory of the project
        final File directory = git.getRepository().getDirectory().getParentFile();

        if (isSet(this.pactGit.tag())) {
            this.gitOperations.checkoutTag(git, getResolvedValue(this.pactGit.tag()));
        } else {

            final String branch = getResolvedValue(this.pactGit.branch());
            if (this.gitOperations.isLocalBranch(git, branch)) {
                this.gitOperations.checkoutBranch(git, branch);
            } else {
                this.gitOperations.checkoutBranch(git, branch, getResolvedValue(this.pactGit.remote()));
            }
        }

        if (isSet(this.pactGit.pactDirectory())) {
            return Paths.get(directory.getAbsolutePath(), getResolvedValue(this.pactGit.pactDirectory()));
        }

        return Paths.get(directory.getAbsolutePath());
    }

    private Git executeClone(Path repository) {
        Git git;
        if (isSet(this.pactGit.username()) && isSet(this.pactGit.passphrase())) {

            git = this.gitOperations.cloneRepository(
                    getResolvedValue(this.pactGit.value()),
                    repository,
                    getResolvedValue(this.pactGit.username()),
                    getResolvedValue(this.pactGit.password())
            );
        } else {
            if (isSet(this.pactGit.passphrase())) {

                git = this.gitOperations.cloneRepository(
                        getResolvedValue(this.pactGit.value()),
                        repository,
                        getResolvedValue(this.pactGit.passphrase()),
                        getPrivateKey());
            } else {

                git = this.gitOperations.cloneRepository(
                        getResolvedValue(this.pactGit.value()),
                        repository);
            }
        }
        return git;
    }

    private PullResult executePull(Git git) {
        final PullResult pullResult;
        if (isSet(this.pactGit.username()) && isSet(this.pactGit.passphrase())) {

            pullResult = this.gitOperations.pullRepository(git,
                    getResolvedValue(this.pactGit.remote()),
                    getResolvedValue(this.pactGit.branch()),
                    getResolvedValue(this.pactGit.username()),
                    getResolvedValue(this.pactGit.password()));
        } else {
            if (isSet(this.pactGit.passphrase())) {

                pullResult = this.gitOperations.pullRepository(git,
                        getResolvedValue(this.pactGit.remote()),
                        getResolvedValue(this.pactGit.branch()),
                        getResolvedValue(this.pactGit.passphrase()),
                        getPrivateKey());
            } else {

                pullResult = this.gitOperations.pullRepository(git,
                        getResolvedValue(this.pactGit.remote()),
                        getResolvedValue(this.pactGit.branch()));
            }
        }
        return pullResult;
    }

    private Path getPrivateKey() {
        if (isSet(this.pactGit.key())) {
            return Paths.get(getResolvedValue(resolveHomeDirectory(this.pactGit.key())));
        }

        return null;
    }

    private boolean isSet(String field) {
        return !"".equals(field);
    }

    private String getResolvedValue(String field) {
        return PactRunnerExpressionParser.parseExpressions(field);
    }

    public static String resolveHomeDirectory(String path) {
        if(path.startsWith("~")) {
            return path.replace("~", System.getProperty("user.home"));
        }
        return path;
    }
}
