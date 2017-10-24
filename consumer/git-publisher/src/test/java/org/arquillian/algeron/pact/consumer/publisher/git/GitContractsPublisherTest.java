package org.arquillian.algeron.pact.consumer.publisher.git;

import org.arquillian.algeron.git.GitOperations;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitContractsPublisherTest {

    @Mock
    GitOperations gitOperations;

    @Mock
    Git git;

    @Mock
    Repository repository;

    @Mock
    PullResult pullResult;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_clone_and_copy_pact_files_and_push() throws IOException {

        byte[] content = "Contract File".getBytes();
        final File contractLocation = temporaryFolder.newFolder("input");
        Files.copy(new ByteArrayInputStream(content),
            new File(contractLocation, "pact.txt").toPath());

        GitContractsPublisher gitContractsPublisher = new GitContractsPublisher();
        gitContractsPublisher.gitOperations = gitOperations;

        File repository = temporaryFolder.newFolder("repo");
        final Map<String, Object> config = getConfigurationWithCommonFields(repository, contractLocation);
        gitContractsPublisher.configure(config);

        when(gitOperations.isValidGitRepository(Paths.get(repository.getAbsolutePath()))).thenReturn(false);
        when(gitOperations.cloneRepository("myurl", Paths.get(repository.getAbsolutePath()))).thenReturn(git);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(true);

        when(git.getRepository()).thenReturn(this.repository);
        when(this.repository.getDirectory()).thenReturn(new File(repository, ".git"));

        gitContractsPublisher.publish();
        verify(gitOperations).addAndCommit(git, "my comment");
        verify(gitOperations).pushToRepository(git, "origin");

        assertThat(new File(repository, "pact.txt")).exists().hasContent("Contract File");
    }

    @Test
    public void should_reuse_git_repo_copy_pact_files_and_push() throws IOException {

        byte[] content = "Contract File".getBytes();
        final File contractLocation = temporaryFolder.newFolder("input");
        Files.copy(new ByteArrayInputStream(content),
            new File(contractLocation, "pact.txt").toPath());

        GitContractsPublisher gitContractsPublisher = new GitContractsPublisher();
        gitContractsPublisher.gitOperations = gitOperations;

        File repository = temporaryFolder.newFolder("repo");
        final Map<String, Object> config = getConfigurationWithCommonFields(repository, contractLocation);
        gitContractsPublisher.configure(config);

        when(git.getRepository()).thenReturn(this.repository);

        when(gitOperations.isValidGitRepository(Paths.get(repository.getAbsolutePath()))).thenReturn(true);
        when(gitOperations.openGitRepository(Paths.get(repository.getAbsolutePath()))).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(this.repository)).thenReturn(true);
        when(pullResult.isSuccessful()).thenReturn(true);
        when(gitOperations.pullFromRepository(git, "origin", "master")).thenReturn(pullResult);

        when(gitOperations.isLocalBranch(git, "master")).thenReturn(true);

        when(this.repository.getDirectory()).thenReturn(new File(repository, ".git"));

        gitContractsPublisher.publish();
        verify(gitOperations).addAndCommit(git, "my comment");
        verify(gitOperations).pushToRepository(git, "origin");

        assertThat(new File(repository, "pact.txt")).exists().hasContent("Contract File");
    }

    @Test
    public void should_reuse_git_repo_and_tag_if_required_and_push() throws IOException {

        byte[] content = "Contract File".getBytes();
        final File contractLocation = temporaryFolder.newFolder("input");
        Files.copy(new ByteArrayInputStream(content),
            new File(contractLocation, "pact.txt").toPath());

        GitContractsPublisher gitContractsPublisher = new GitContractsPublisher();
        gitContractsPublisher.gitOperations = gitOperations;

        File repository = temporaryFolder.newFolder("repo");
        final Map<String, Object> config = getConfigurationWithCommonFields(repository, contractLocation);
        config.put(GitContractsPublisher.TAG, "mytag");
        gitContractsPublisher.configure(config);

        when(git.getRepository()).thenReturn(this.repository);

        when(gitOperations.isValidGitRepository(Paths.get(repository.getAbsolutePath()))).thenReturn(true);
        when(gitOperations.openGitRepository(Paths.get(repository.getAbsolutePath()))).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(this.repository)).thenReturn(true);
        when(pullResult.isSuccessful()).thenReturn(true);
        when(gitOperations.pullFromRepository(git, "origin", "master")).thenReturn(pullResult);

        when(gitOperations.isLocalBranch(git, "master")).thenReturn(true);

        when(this.repository.getDirectory()).thenReturn(new File(repository, ".git"));

        gitContractsPublisher.publish();
        verify(gitOperations).addAndCommit(git, "my comment");
        verify(gitOperations).createTag(git, "mytag");
        verify(gitOperations).pushToRepository(git, "origin");

        assertThat(new File(repository, "pact.txt")).exists().hasContent("Contract File");
    }

    @Test
    public void should_reuse_git_repo_creating_branch_if_not_exist_copy_pact_files_and_push() throws IOException {

        byte[] content = "Contract File".getBytes();
        final File contractLocation = temporaryFolder.newFolder("input");
        Files.copy(new ByteArrayInputStream(content),
            new File(contractLocation, "pact.txt").toPath());

        GitContractsPublisher gitContractsPublisher = new GitContractsPublisher();
        gitContractsPublisher.gitOperations = gitOperations;

        File repository = temporaryFolder.newFolder("repo");
        final Map<String, Object> config = getConfigurationWithCommonFields(repository, contractLocation);
        config.put(GitContractsPublisher.BRANCH, "mybranch");
        gitContractsPublisher.configure(config);

        when(git.getRepository()).thenReturn(this.repository);

        when(gitOperations.isValidGitRepository(Paths.get(repository.getAbsolutePath()))).thenReturn(true);
        when(gitOperations.openGitRepository(Paths.get(repository.getAbsolutePath()))).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(this.repository)).thenReturn(true);
        when(pullResult.isSuccessful()).thenReturn(true);
        when(gitOperations.pullFromRepository(git, "origin", "mybranch")).thenReturn(pullResult);

        when(gitOperations.isLocalBranch(git, "mybranch")).thenReturn(false);
        when(gitOperations.isRemoteBranch(git, "mybranch", "origin")).thenReturn(false);

        when(this.repository.getDirectory()).thenReturn(new File(repository, ".git"));

        gitContractsPublisher.publish();
        verify(gitOperations).createBranchAndCheckout(git, "mybranch");
    }

    private Map<String, Object> getConfigurationWithCommonFields(File repository, File contractLocation) {
        Map<String, Object> config = new HashMap<>();
        config.put(GitContractsPublisher.URL, "myurl");
        config.put(GitContractsPublisher.COMMENT, "my comment");
        config.put(GitContractsPublisher.REPOSITORY, repository.getAbsolutePath());
        config.put(GitContractsPublisher.CONTRACTS_FOLDER, contractLocation.getAbsolutePath());

        return config;
    }
}
