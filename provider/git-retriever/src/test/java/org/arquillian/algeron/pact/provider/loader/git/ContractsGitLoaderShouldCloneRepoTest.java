package org.arquillian.algeron.pact.provider.loader.git;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.arquillian.algeron.git.GitOperations;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractsGitLoaderShouldCloneRepoTest {

    @Mock
    GitOperations gitOperations;

    @Mock
    Git git;

    @Mock
    Repository repository;

    @Before
    public void setup() {
        when(git.getRepository()).thenReturn(repository);
        when(repository.getDirectory()).thenReturn(new File("/tmp/.git"));
    }

    @Test
    public void should_clone_repo() throws IOException {
        when(gitOperations.cloneRepository(eq("myrepourl"), any(Path.class))).thenReturn(git);
        when(gitOperations.isLocalBranch(git, "master")).thenReturn(false);

        ContractsGitLoader pactGitLoader =
            new ContractsGitLoader(TestWithPactRandomDirectory.class.getAnnotation(ContractsGit.class));
        pactGitLoader.gitOperations = gitOperations;

        pactGitLoader.getContractsFolderFromGitRepo();

        verify(gitOperations).cloneRepository(eq("myrepourl"), any(Path.class));
        verify(gitOperations).checkoutBranch(git, "master", "origin");
    }

    @ContractsGit("myrepourl")
    private static class TestWithPactRandomDirectory {
    }

}
