package org.arquillian.pact.common.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GitOperationsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_check_if_branch_is_local() throws IOException {
        GitOperations gitOperations = new GitOperations();
        Git git = gitOperations.cloneRepository("https://github.com/github/testrepo.git",
                Paths.get(temporaryFolder.newFolder().getAbsolutePath()));

        assertThat(gitOperations.isLocalBranch(git, "master")).isTrue();

    }

    @Test
    public void should_check_if_branch_is_remote() throws IOException, GitAPIException {
        GitOperations gitOperations = new GitOperations();
        Git git = gitOperations.cloneRepository("https://github.com/github/testrepo.git",
                Paths.get(temporaryFolder.newFolder().getAbsolutePath()));

        assertThat(gitOperations.isRemoteBranch(git, "email", "origin"));

    }

    @Test
    public void should_create_and_checkout_branch() throws IOException, GitAPIException {
        GitOperations gitOperations = new GitOperations();
        Git git = gitOperations.cloneRepository("https://github.com/github/testrepo.git",
                Paths.get(temporaryFolder.newFolder().getAbsolutePath()));

        gitOperations.createBranchAndCheckout(git, "mytest");
        final String fullBranch = git.getRepository().getFullBranch();

        assertThat(fullBranch).isEqualTo("refs/heads/mytest");

    }

    @Test
    public void should_add_and_commit_new_files() throws IOException, GitAPIException {
        GitOperations gitOperations = new GitOperations();
        Git git = gitOperations.cloneRepository("https://github.com/github/testrepo.git",
                Paths.get(temporaryFolder.newFolder().getAbsolutePath()));

        byte[] content = "Contract File".getBytes();
        Files.copy(new ByteArrayInputStream(content),
                new File(git.getRepository().getDirectory().getParentFile(), "pact.txt").toPath());

        final RevCommit revCommit = gitOperations.addAndCommit(git, "New Commit");
        assertThat(revCommit.getShortMessage()).isEqualTo("New Commit");
        assertThat(git.status().call().getUntracked()).hasSize(0);
    }

    @Test
    public void should_create_tags() throws IOException, GitAPIException {
        GitOperations gitOperations = new GitOperations();
        Git git = gitOperations.cloneRepository("https://github.com/github/testrepo.git",
                Paths.get(temporaryFolder.newFolder().getAbsolutePath()));

        Ref ref = gitOperations.createTag(git, "mytag");
        assertThat(git.tagList().call()).contains(ref);
    }

}
