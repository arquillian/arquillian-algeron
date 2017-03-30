package org.arquillian.algeron.pact.provider.loader.git;

import org.arquillian.algeron.git.GitOperations;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContractsGitLoaderTest {

    @Mock
    GitOperations gitOperations;

    @Mock
    Git git;

    @Mock
    PullResult pullResult;

    @Mock
    Repository repository;

    @Before
    public void setup() {
        when(git.getRepository()).thenReturn(repository);
        when(pullResult.isSuccessful()).thenReturn(true);
        when(repository.getDirectory()).thenReturn(new File("/tmp/.git"));
    }

    @Test
    public void should_clone_repo() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.cloneRepository(eq("myrepourl"), any(Path.class))).thenReturn(git);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        ContractsGitLoader pactGitLoader =
            new ContractsGitLoader(TestWithPactRandomDirectory.class.getAnnotation(ContractsGit.class));
        pactGitLoader.gitOperations = gitOperations;

        pactGitLoader.getContractsFolderFromGitRepo();

        verify(gitOperations).cloneRepository(eq("myrepourl"), any(Path.class));
        verify(gitOperations).checkoutBranch(git, "master", "origin");
    }

    @Test
    public void should_pull_existing_git_repo_local_branch() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullFromRepository(git, "origin", "master")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(true);

        ContractsGitLoader pactGitLoader =
            new ContractsGitLoader(TestWithPactRepositoryDirectory.class.getAnnotation(ContractsGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getContractsFolderFromGitRepo();

        verify(gitOperations).pullFromRepository(git, "origin", "master");
        verify(gitOperations).checkoutBranch(git, "master");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp");
    }

    @Test
    public void should_pull_existing_git_repo() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullFromRepository(git, "origin", "master")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        ContractsGitLoader pactGitLoader =
            new ContractsGitLoader(TestWithPactRepositoryDirectory.class.getAnnotation(ContractsGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getContractsFolderFromGitRepo();

        verify(gitOperations).pullFromRepository(git, "origin", "master");
        verify(gitOperations).checkoutBranch(git, "master", "origin");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp");
    }

    @Test
    public void should_pull_existing_git_repo_and_move_to_subfolder() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullFromRepository(git, "origin", "master")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        ContractsGitLoader pactGitLoader =
            new ContractsGitLoader(TestWithPactRepositoryDirectoryAndSubfolder.class.getAnnotation(ContractsGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getContractsFolderFromGitRepo();

        verify(gitOperations).pullFromRepository(git, "origin", "master");
        verify(gitOperations).checkoutBranch(git, "master", "origin");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp/pacts");
    }

    @Test
    public void should_pull_specific_branch_git_repo_and_use_tag() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullFromRepository(git, "origin", "mybranch")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        ContractsGitLoader pactGitLoader =
            new ContractsGitLoader(TestWithPactRepositoryDirectoryAndTag.class.getAnnotation(ContractsGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getContractsFolderFromGitRepo();

        verify(gitOperations).pullFromRepository(git, "origin", "mybranch");
        verify(gitOperations).checkoutTag(git, "mytag");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp");
    }

    @ContractsGit("myrepourl")
    private static class TestWithPactRandomDirectory {
    }

    @ContractsGit(value = "", repository = "/tmp")
    private static class TestWithPactRepositoryDirectory {
    }

    @ContractsGit(value = "", repository = "/tmp", tag = "mytag", branch = "mybranch")
    private static class TestWithPactRepositoryDirectoryAndTag {
    }

    @ContractsGit(value = "", repository = "/tmp", contractsDirectory = "pacts")
    private static class TestWithPactRepositoryDirectoryAndSubfolder {
    }
}
