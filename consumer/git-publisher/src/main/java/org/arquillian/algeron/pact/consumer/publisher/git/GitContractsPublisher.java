package org.arquillian.algeron.pact.consumer.publisher.git;

import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher;
import org.arquillian.algeron.git.GitOperations;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.arquillian.algeron.configuration.HomeResolver.resolveHomeDirectory;

public class GitContractsPublisher implements ContractsPublisher {

    static final String URL = "url";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String PASSPHRASE = "passphrase";
    static final String REMOTE = "remote";
    static final String KEY = "key";
    static final String REPOSITORY = "repository";
    static final String CONTRACT_GIT_DIRECTORY = "contractGitDirectory";
    static final String TAG = "tag";
    static final String BRANCH = "branch";
    static final String COMMENT = "comment";
    static final String EMAIL = "email";
    static final String CONTRACTS_FOLDER = "contractsFolder";

    private static final Logger logger = Logger.getLogger(GitContractsPublisher.class.getName());

    private Map<String, Object> configuration;
    GitOperations gitOperations;


    public GitContractsPublisher() {
        this.gitOperations = new GitOperations();
    }

    @Override
    public void publish() throws IOException {

        Git git = null;
        try {
            git = getGitRepositoryWithLatestRemoteChanges(git);

            logger.log(Level.INFO, String.format("Git repository with contract files at %s.", git.getRepository().getDirectory()
                    .getParentFile().getAbsolutePath()));

            // Now repository structure is created and we can start operating on it
            final Path outputLocation = moveToCorrectLocation(git);
            final String contractFolder = (String) this.configuration.get(CONTRACTS_FOLDER);
            final Path contractsSource = Paths.get(RunnerExpressionParser.parseExpressions(contractFolder));
            final List<Path> contractFiles = copyContractFiles(contractsSource, outputLocation);
            executeCommitAndPush(git);

            logger.log(Level.INFO, String.format("Contract files %s pushed to %s repository.",
                    contractFiles.stream()
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.joining(System.lineSeparator(), "[", "]")),
                    configuration.get(URL)));

        } finally {
            if (git != null) {
                git.close();
            }
        }

    }

    protected Git getGitRepositoryWithLatestRemoteChanges(Git git) throws IOException {
        if (isSet(REPOSITORY, String.class, this.configuration)) {

            Path repository = Paths.get(getResolvedValue((String) this.configuration.get(REPOSITORY)));

            if (this.gitOperations.isValidGitRepository(repository)) {
                git = useLocalGitRepository(repository);
            } else {
                logger.log(Level.INFO, String.format("%s directory is not a git directory or does not exists and it is going to be deleted and cloned", repository));

                Files.deleteIfExists(repository);
                Files.createDirectories(repository);
                git = executeClone(repository);
            }

        } else {
            // Put files in a temp directory
            final Path testGitRepository = Files.createTempDirectory("TestGitRepository");

            logger.info(String.format("Repository is going to be cloned at %s", testGitRepository));

            git = executeClone(testGitRepository);
        }
        return git;
    }

    protected Git useLocalGitRepository(Path repository) throws IOException {
        Git git;
        git = this.gitOperations.openGitRepository(repository);
        if (this.gitOperations.hasAtLeastOneReference(git.getRepository())) {

            final PullResult pullResult = executePull(git);

            if (!pullResult.isSuccessful()) {
                // Merge conflicts
                throw new IllegalArgumentException("There are merge conflicts into an existing git repo. Provider should not deal with merge conflicts. Correct them or delete the repo and execute again the test.");
            }
        } else {
            throw new IllegalArgumentException(String.format("Git repository %s was not cloned correctly.", git.getRepository().getDirectory().getAbsolutePath()));
        }
        return git;
    }

    private void executeCommitAndPush(Git git) {
        executeCommit(git);
        executeTag(git);
        executePush(git);
    }

    private void executeTag(Git git) {

        if (isSet(TAG, String.class, this.configuration)) {
            this.gitOperations.createTag(git, getResolvedValue((String) this.configuration.get(TAG)));
        }
    }

    private void executePush(Git git) {
        if (isSet(USERNAME, String.class, this.configuration) && isSet(PASSWORD, String.class, this.configuration)) {

            this.gitOperations.pushToRepository(git,
                    getResolvedValue((String) this.configuration.get(REMOTE)),
                    getResolvedValue((String) this.configuration.get(USERNAME)),
                    getResolvedValue((String) this.configuration.get(PASSWORD))
            );
        } else {
            if (isSet(PASSPHRASE, String.class, this.configuration)) {

                this.gitOperations.pushToRepository(git,
                        getResolvedValue((String) this.configuration.get(REMOTE)),
                        getResolvedValue((String) this.configuration.get(PASSPHRASE)),
                        getPrivateKey());
            } else {

                this.gitOperations.pushToRepository(git,
                        getResolvedValue((String) this.configuration.get(REMOTE))
                );
            }
        }
    }

    private void executeCommit(Git git) {
        if (isSet(USERNAME, String.class, this.configuration) && isSet(EMAIL, String.class, this.configuration)) {
            this.gitOperations.addAndCommit(git,
                    getResolvedValue((String) this.configuration.get(COMMENT)),
                    getResolvedValue((String) this.configuration.get(USERNAME)),
                    getResolvedValue((String) this.configuration.get(EMAIL))
            );
        } else {
            this.gitOperations.addAndCommit(git,
                    getResolvedValue((String) this.configuration.get(COMMENT)));
        }
    }

    private List<Path> copyContractFiles(Path contractsLocation, Path outputPath) throws IOException {
        final List<Path> contractFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(contractsLocation)) {
            stream
                    .filter(path -> !Files.isDirectory(path))
                    .peek(path -> contractFiles.add(path))
                    .forEach(path -> {
                try {
                    final Path contractFile = outputPath.resolve(path.getFileName());
                    Files.copy(path, contractFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            });
        }

        return contractFiles;
    }

    private Git executeClone(Path repository) {
        Git git;
        if (isSet(USERNAME, String.class, this.configuration) && isSet(PASSWORD, String.class, this.configuration)) {

            git = this.gitOperations.cloneRepository(
                    getResolvedValue((String) this.configuration.get(URL)),
                    repository,
                    getResolvedValue((String) this.configuration.get(USERNAME)),
                    getResolvedValue((String) this.configuration.get(PASSWORD))
            );
        } else {
            if (isSet(PASSPHRASE, String.class, this.configuration)) {

                git = this.gitOperations.cloneRepository(
                        getResolvedValue((String) this.configuration.get(URL)),
                        repository,
                        getResolvedValue((String) this.configuration.get(PASSPHRASE)),
                        getPrivateKey());
            } else {

                git = this.gitOperations.cloneRepository(
                        getResolvedValue((String) this.configuration.get(URL)),
                        repository);
            }
        }
        return git;
    }

    private PullResult executePull(Git git) {
        final PullResult pullResult;
        if (isSet(USERNAME, String.class, this.configuration) && isSet(PASSWORD, String.class, this.configuration)) {

            pullResult = this.gitOperations.pullFromRepository(git,
                    getResolvedValue((String) this.configuration.get(REMOTE)),
                    getResolvedValue((String) this.configuration.get(BRANCH)),
                    getResolvedValue((String) this.configuration.get(USERNAME)),
                    getResolvedValue((String) this.configuration.get(PASSWORD))
            );
        } else {
            if (isSet(PASSPHRASE, String.class, this.configuration)) {

                pullResult = this.gitOperations.pullFromRepository(git,
                        getResolvedValue((String) this.configuration.get(REMOTE)),
                        getResolvedValue((String) this.configuration.get(BRANCH)),
                        getResolvedValue((String) this.configuration.get(PASSPHRASE)),
                        getPrivateKey());
            } else {

                pullResult = this.gitOperations.pullFromRepository(git,
                        getResolvedValue((String) this.configuration.get(REMOTE)),
                        getResolvedValue((String) this.configuration.get(BRANCH)));
            }
        }
        return pullResult;
    }

    private Path moveToCorrectLocation(final Git git) {
        final File directory = git.getRepository().getDirectory().getParentFile();

        final String branch = getResolvedValue((String) this.configuration.get(BRANCH));
        if (this.gitOperations.isLocalBranch(git, branch)) {
            this.gitOperations.checkoutBranch(git, branch);
        } else {
            final String remoteBranch = getResolvedValue((String) this.configuration.get(REMOTE));
            if (this.gitOperations.isRemoteBranch(git, branch, remoteBranch)) {
                this.gitOperations.checkoutBranch(git, branch, remoteBranch);
            } else {
                //if not local nor remote means branch needs to be created
                this.gitOperations.createBranchAndCheckout(git, branch);
            }
        }

        if (isSet(CONTRACT_GIT_DIRECTORY, String.class, this.configuration)) {
            return Paths.get(directory.getAbsolutePath(), getResolvedValue((String) this.configuration.get(CONTRACT_GIT_DIRECTORY)));
        }

        return Paths.get(directory.getAbsolutePath());
    }

    private Path getPrivateKey() {
        if (isSet(KEY, String.class, this.configuration)) {
            return Paths.get(getResolvedValue(resolveHomeDirectory((String) this.configuration.get(KEY))));
        }

        return null;
    }

    @Override
    public String getName() {
        return "git";
    }

    @Override
    public void configure(Map<String, Object> configuration) {
        this.configuration = configuration;

        if (!this.configuration.containsKey(URL)) {
            throw new IllegalArgumentException(String.format("To use Git Publisher you need to set %s of the repository", URL));
        }

        if (!(this.configuration.get(URL) instanceof String)) {
            throw new IllegalArgumentException(String.format("Git Publisher requires %s configuration property to be an String instead of %s", URL, this.configuration.get(URL)));
        }

        if (!this.configuration.containsKey(COMMENT)) {
            throw new IllegalArgumentException(String.format("To use Git Publisher you need to set %s of the repository", COMMENT));
        }

        if (!(this.configuration.get(COMMENT) instanceof String)) {
            throw new IllegalArgumentException(String.format("Git Publisher requires %s configuration property to be an String instead of %s", COMMENT, this.configuration.get(COMMENT)));
        }

        if (!this.configuration.containsKey(CONTRACTS_FOLDER)) {
            throw new IllegalArgumentException(String.format("Git Publisher requires %s configuration property", CONTRACTS_FOLDER));
        }

        if (!(this.configuration.get(CONTRACTS_FOLDER) instanceof String)) {
            throw new IllegalArgumentException(String.format("Git Publisher requires %s configuration property to be an String", CONTRACTS_FOLDER));
        }

        setDefaults();
    }

    private void setDefaults() {

        if (isNotSet(REMOTE, this.configuration)) {
            this.configuration.put(REMOTE, "origin");
        }

        if (isNotSet(KEY, this.configuration)) {
            this.configuration.put(KEY, System.getProperty("user.home") + "/.ssh/id_rsa");
        }

        if (isNotSet(BRANCH, this.configuration)) {
            this.configuration.put(BRANCH, "master");
        }

    }

    private boolean isNotSet(String field, Map<String, Object> configuration) {
        return !configuration.containsKey(field);
    }

    private boolean isSet(String field, Class<?> expectedType, Map<String, Object> configuration) {

        if (configuration.containsKey(field)) {
            if (expectedType.isAssignableFrom(configuration.get(field).getClass())) {
                return true;
            }
        }

        return false;
    }

    private String getResolvedValue(String field) {
        return RunnerExpressionParser.parseExpressions(field);
    }
}
