package org.arquillian.pact.consumer.publisher.git;

import org.arquillian.pact.common.configuration.PactRunnerExpressionParser;
import org.arquillian.pact.common.git.GitOperations;
import org.arquillian.pact.consumer.spi.publisher.PactPublisher;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class GitPactPublisher implements PactPublisher {

    static final String URL = "url";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String PASSPHRASE = "passphrase";
    static final String REMOTE = "remote";
    static final String KEY = "key";
    static final String REPOSITORY = "repository";
    static final String PACT_DIRECTORY = "pactDirectory";
    static final String TAG = "tag";
    static final String BRANCH = "branch";
    static final String COMMENT = "comment";
    static final String EMAIL = "email";

    private static final Logger logger = Logger.getLogger(GitPactPublisher.class.getName());

    private Map<String, Object> configuration;
    GitOperations gitOperations;


    public GitPactPublisher() {
        this.gitOperations = new GitOperations();
    }

    @Override
    public void store(Path pactsLocation) {

        Git git = null;
        try {
            if (isSet(REPOSITORY, String.class, this.configuration)) {

                Path repository = Paths.get(getResolvedValue((String) this.configuration.get(REPOSITORY)));

                if (this.gitOperations.isValidGitRepository(repository)) {

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

            // Now repository structure is created and we can start operating on it
            final Path outputLocation = moveToCorrectLocation(git);
            copyPactFiles(pactsLocation, outputLocation);
            executeCommitAndPush(git);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (git != null) {
                git.close();
            }
        }

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

            this.gitOperations.pushRepository(git,
                    getResolvedValue((String) this.configuration.get(REMOTE)),
                    getResolvedValue((String) this.configuration.get(USERNAME)),
                    getResolvedValue((String) this.configuration.get(PASSWORD))
            );
        } else {
            if (isSet(PASSPHRASE, String.class, this.configuration)) {

                this.gitOperations.pushRepository(git,
                        getResolvedValue((String) this.configuration.get(REMOTE)),
                        getResolvedValue((String) this.configuration.get(PASSPHRASE)),
                        getPrivateKey());
            } else {

                this.gitOperations.pushRepository(git,
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

    private void copyPactFiles(Path pactsLocation, Path outputPath) throws IOException {
        try (Stream<Path> stream = Files.walk(pactsLocation)) {
            stream.forEach(path -> {
                try {
                    if (!Files.isDirectory(path)) {
                        final Path pactFile = outputPath.resolve(path.getFileName());
                        Files.copy(path, pactFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            });
        }
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

            pullResult = this.gitOperations.pullRepository(git,
                    getResolvedValue((String) this.configuration.get(REMOTE)),
                    getResolvedValue((String) this.configuration.get(BRANCH)),
                    getResolvedValue((String) this.configuration.get(USERNAME)),
                    getResolvedValue((String) this.configuration.get(PASSWORD))
            );
        } else {
            if (isSet(PASSPHRASE, String.class, this.configuration)) {

                pullResult = this.gitOperations.pullRepository(git,
                        getResolvedValue((String) this.configuration.get(REMOTE)),
                        getResolvedValue((String) this.configuration.get(BRANCH)),
                        getResolvedValue((String) this.configuration.get(PASSPHRASE)),
                        getPrivateKey());
            } else {

                pullResult = this.gitOperations.pullRepository(git,
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

        if (isSet(PACT_DIRECTORY, String.class, this.configuration)) {
            return Paths.get(directory.getAbsolutePath(), getResolvedValue((String) this.configuration.get(PACT_DIRECTORY)));
        }

        return Paths.get(directory.getAbsolutePath());
    }

    private Path getPrivateKey() {
        if (isSet(KEY, String.class, this.configuration)) {
            return Paths.get(getResolvedValue((String) this.configuration.get(BRANCH)));
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
            throw new IllegalArgumentException(String.format("To use Git Pact Publisher you need to set %s of the repository", URL));
        }

        if (!(this.configuration.get(URL) instanceof String)) {
            throw new IllegalArgumentException(String.format("Git Pact Publisher requires %s configuration property to be an String instead of %s", URL, this.configuration.get(URL)));
        }

        if (!this.configuration.containsKey(COMMENT)) {
            throw new IllegalArgumentException(String.format("To use Git Pact Publisher you need to set %s of the repository", COMMENT));
        }

        if (!(this.configuration.get(COMMENT) instanceof String)) {
            throw new IllegalArgumentException(String.format("Git Pact Publisher requires %s configuration property to be an String instead of %s", COMMENT, this.configuration.get(COMMENT)));
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
        return PactRunnerExpressionParser.parseExpressions(field);
    }
}
