package org.arquillian.algeron.pact.provider.loader.git;

import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.git.GitOperations;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.arquillian.algeron.configuration.HomeResolver.resolveHomeDirectory;

/**
 * Loader to get contract files from git repository.
 */
public class ContractsGitLoader implements ContractsRetriever {

    private final static Logger logger = Logger.getLogger(ContractsGitLoader.class.getName());

    private ContractsGit contractsGit;

    GitOperations gitOperations;

    public ContractsGitLoader() {
    }

    public ContractsGitLoader(ContractsGit contractsGit) {
        this.contractsGit = contractsGit;
        this.gitOperations = new GitOperations();
    }

    @Override
    public List<URI> retrieve() throws IOException {
        final Path contractsFolderFromGitRepo = getContractsFolderFromGitRepo();
        return Arrays.stream(contractsFolderFromGitRepo.toFile().listFiles())
                .map(file -> "file://" + file.getAbsolutePath())
                .map(URI::create)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "git";
    }

    @Override
    public void configure(Map<String, Object> configuration) {
        this.contractsGit = new ExternallyConfiguredContractsGit(configuration);
    }


    protected Path getContractsFolderFromGitRepo() throws IOException {
        Path location = null;
        Git git = null;
        try {
            if (isSet(this.contractsGit.repository())) {

                Path repository = Paths.get(getResolvedValue(this.contractsGit.repository()));

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

                logger.log(Level.INFO, String.format("Repository is going to be cloned at %s", testGitRepository));

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

        if (isSet(this.contractsGit.tag())) {
            this.gitOperations.checkoutTag(git, getResolvedValue(this.contractsGit.tag()));
        } else {

            final String branch = getResolvedValue(this.contractsGit.branch());
            if (this.gitOperations.isLocalBranch(git, branch)) {
                this.gitOperations.checkoutBranch(git, branch);
            } else {
                this.gitOperations.checkoutBranch(git, branch, getResolvedValue(this.contractsGit.remote()));
            }
        }

        if (isSet(this.contractsGit.contractsDirectory())) {
            return Paths.get(directory.getAbsolutePath(), getResolvedValue(this.contractsGit.contractsDirectory()));
        }

        return Paths.get(directory.getAbsolutePath());
    }

    private Git executeClone(Path repository) {
        Git git;
        if (isSet(this.contractsGit.username()) && isSet(this.contractsGit.password())) {

            git = this.gitOperations.cloneRepository(
                    getResolvedValue(this.contractsGit.value()),
                    repository,
                    getResolvedValue(this.contractsGit.username()),
                    getResolvedValue(this.contractsGit.password())
            );
        } else {
            if (isSet(this.contractsGit.passphrase())) {

                git = this.gitOperations.cloneRepository(
                        getResolvedValue(this.contractsGit.value()),
                        repository,
                        getResolvedValue(this.contractsGit.passphrase()),
                        getPrivateKey());
            } else {

                git = this.gitOperations.cloneRepository(
                        getResolvedValue(this.contractsGit.value()),
                        repository);
            }
        }
        return git;
    }

    private PullResult executePull(Git git) {
        final PullResult pullResult;
        if (isSet(this.contractsGit.username()) && isSet(this.contractsGit.password())) {

            pullResult = this.gitOperations.pullFromRepository(git,
                    getResolvedValue(this.contractsGit.remote()),
                    getResolvedValue(this.contractsGit.branch()),
                    getResolvedValue(this.contractsGit.username()),
                    getResolvedValue(this.contractsGit.password()));
        } else {
            if (isSet(this.contractsGit.passphrase())) {

                pullResult = this.gitOperations.pullFromRepository(git,
                        getResolvedValue(this.contractsGit.remote()),
                        getResolvedValue(this.contractsGit.branch()),
                        getResolvedValue(this.contractsGit.passphrase()),
                        getPrivateKey());
            } else {

                pullResult = this.gitOperations.pullFromRepository(git,
                        getResolvedValue(this.contractsGit.remote()),
                        getResolvedValue(this.contractsGit.branch()));
            }
        }
        return pullResult;
    }

    private Path getPrivateKey() {
        if (isSet(this.contractsGit.key())) {
            return Paths.get(getResolvedValue(resolveHomeDirectory(this.contractsGit.key())));
        }

        return null;
    }

    private boolean isSet(String field) {
        return !"".equals(field);
    }

    private String getResolvedValue(String field) {
        return RunnerExpressionParser.parseExpressions(field);
    }

    static class ExternallyConfiguredContractsGit implements ContractsGit {

        private static final String URL = "url";
        private static final String USERNAME = "username";
        private static final String PASSWORD = "password";
        private static final String PASSPHRASE = "passphrase";
        private static final String REMOTE = "remote";
        private static final String KEY = "key";
        private static final String REPOSITORY = "repository";
        private static final String CONTRACT_GIT_DIRECTORY = "contractGitDirectory";
        private static final String TAG = "tag";
        private static final String BRANCH = "branch";


        private String url = "";
        private String username= "";
        private String password = "";
        private String passphrase = "";
        private String key = "";
        private String repository = "";
        private String contractsDirectory = "";
        private String tag = "";
        private String branch = "";
        private String remote = "";

        public ExternallyConfiguredContractsGit(Map<String, Object> configuration) {
            if (configuration.containsKey(URL)) {
                url = (String) configuration.get(URL);
            }

            if (configuration.containsKey(USERNAME)) {
                username = (String) configuration.get(USERNAME);
            }

            if (configuration.containsKey(PASSWORD)) {
                password = (String) configuration.get(PASSWORD);
            }

            if (configuration.containsKey(PASSPHRASE)) {
                passphrase = (String) configuration.get(PASSPHRASE);
            }

            if (configuration.containsKey(KEY)) {
                key = (String) configuration.get(KEY);
            }

            if (configuration.containsKey(REPOSITORY)) {
                repository = (String) configuration.get(REPOSITORY);
            }

            if (configuration.containsKey(CONTRACT_GIT_DIRECTORY)) {
                contractsDirectory = (String) configuration.get(CONTRACT_GIT_DIRECTORY);
            }

            if (configuration.containsKey(TAG)) {
                tag = (String) configuration.get(TAG);
            }

            if (configuration.containsKey(BRANCH)) {
                branch = (String) configuration.get(BRANCH);
            }

            if (configuration.containsKey(REMOTE)) {
                remote = (String) configuration.get(REMOTE);
            }

        }

        @Override
        public String value() {
            return url;
        }

        @Override
        public String username() {
            return username;
        }

        @Override
        public String password() {
            return password;
        }

        @Override
        public String passphrase() {
            return passphrase;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public String repository() {
            return repository;
        }

        @Override
        public String contractsDirectory() {
            return contractsDirectory;
        }

        @Override
        public String tag() {
            return tag;
        }

        @Override
        public String branch() {
            return branch;
        }

        @Override
        public String remote() {
            return remote;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ContractsGit.class;
        }
    }

}
