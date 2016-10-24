package org.arquillian.pact.provider.loader.git;

import org.arquillian.pact.common.git.GitOperations;
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
public class PactGitLoaderTest {

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

        PactGitLoader pactGitLoader = new PactGitLoader(TestWithPactRandomDirectory.class.getAnnotation(PactGit.class));
        pactGitLoader.gitOperations = gitOperations;

        pactGitLoader.getPactsFolderFromGitRepo();

        verify(gitOperations).cloneRepository(eq("myrepourl"), any(Path.class));
        verify(gitOperations).checkoutBranch(git, "master", "origin");
    }

    @Test
    public void should_pull_existing_git_repo_local_branch() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullRepository(git, "origin", "master")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(true);

        PactGitLoader pactGitLoader = new PactGitLoader(TestWithPactRepositoryDirectory.class.getAnnotation(PactGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getPactsFolderFromGitRepo();

        verify(gitOperations).pullRepository(git, "origin", "master");
        verify(gitOperations).checkoutBranch(git, "master");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp");

    }

    @Test
    public void should_pull_existing_git_repo() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullRepository(git, "origin", "master")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        PactGitLoader pactGitLoader = new PactGitLoader(TestWithPactRepositoryDirectory.class.getAnnotation(PactGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getPactsFolderFromGitRepo();

        verify(gitOperations).pullRepository(git, "origin", "master");
        verify(gitOperations).checkoutBranch(git, "master", "origin");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp");

    }

    @Test
    public void should_pull_existing_git_repo_and_move_to_subfolder() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullRepository(git, "origin", "master")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        PactGitLoader pactGitLoader = new PactGitLoader(TestWithPactRepositoryDirectoryAndSubfolder.class.getAnnotation(PactGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getPactsFolderFromGitRepo();

        verify(gitOperations).pullRepository(git, "origin", "master");
        verify(gitOperations).checkoutBranch(git, "master", "origin");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp/pacts");

    }

    @Test
    public void should_pull_specific_branch_git_repo_and_use_tag() throws IOException {
        final Path path = Paths.get("/tmp");
        when(gitOperations.isValidGitRepository(path)).thenReturn(true);
        when(gitOperations.openGitRepository(path)).thenReturn(git);
        when(gitOperations.hasAtLeastOneReference(repository)).thenReturn(true);

        when(gitOperations.pullRepository(git, "origin", "mybranch")).thenReturn(pullResult);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        PactGitLoader pactGitLoader = new PactGitLoader(TestWithPactRepositoryDirectoryAndTag.class.getAnnotation(PactGit.class));
        pactGitLoader.gitOperations = gitOperations;

        final Path pactsFromGitRepo = pactGitLoader.getPactsFolderFromGitRepo();

        verify(gitOperations).pullRepository(git, "origin", "mybranch");
        verify(gitOperations).checkoutTag(git, "mytag");
        assertThat(pactsFromGitRepo.toString()).isEqualTo("/tmp");

    }


    @PactGit("myrepourl")
    private static class TestWithPactRandomDirectory {
    }

    @PactGit(value= "", repository = "/tmp")
    private static class TestWithPactRepositoryDirectory {
    }

    @PactGit(value= "", repository = "/tmp", tag = "mytag", branch = "mybranch")
    private static class TestWithPactRepositoryDirectoryAndTag {
    }

    @PactGit(value= "", repository = "/tmp", pactDirectory = "pacts")
    private static class TestWithPactRepositoryDirectoryAndSubfolder {
    }

}
