package org.arquillian.algeron.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GitOperations {

    /**
     * Checks if given folder is a git repository
     *
     * @param folder to check
     * @return true if it is a git repository, false otherwise.
     */
    public boolean isValidGitRepository(Path folder) {

        if (Files.exists(folder) && Files.isDirectory(folder)) {

            // If it has been at least initialized
            if (RepositoryCache.FileKey.isGitRepository(folder.toFile(), FS.DETECTED)) {
                // we are assuming that the clone worked at that time, caller should call hasAtLeastOneReference
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    /**
     * Opens local git repository.
     *
     * @param path of git repository
     * @return Git instance
     * @throws IOException
     */
    public Git openGitRepository(Path path) throws IOException {
        return Git.open(path.toFile());
    }

    /**
     * Checkout existing tag.
     *
     * @param git instance.
     * @param tag to move
     * @return Ref to current branch
     */
    public Ref checkoutTag(Git git, String tag) {
        try {
            return git.checkout()
                    .setName("tags/" + tag).call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checks if given branch has been checkedout locally too.
     *
     * @param git    instance.
     * @param branch to check.
     * @return True if it is local, false otherwise.
     */
    public boolean isLocalBranch(final Git git, final String branch) {
        try {
            final List<Ref> refs = git.branchList().call();
            return refs.stream()
                    .anyMatch(ref -> ref.getName().endsWith(branch));
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Checks if given branch is remote.
     *
     * @param git    instance.
     * @param branch to check.
     * @param remote name.
     * @return True if it is remote, false otherwise.
     */
    public boolean isRemoteBranch(final Git git, final String branch, final String remote) {
        try {
            final List<Ref> refs = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE).call();

            final String remoteBranch = remote + "/" + branch;
            return refs.stream().anyMatch(ref -> ref.getName().endsWith(remoteBranch));
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checkout existing branch.
     *
     * @param git    instance.
     * @param branch to move
     * @return Ref to current branch
     */
    public Ref checkoutBranch(Git git, String branch) {
        try {
            return git.checkout()
                    .setName(branch)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checkout existing branch.
     *
     * @param git    instance.
     * @param branch to move
     * @param remote repository name
     * @return Ref to current branch
     */
    public Ref checkoutBranch(Git git, String branch, String remote) {
        try {
            return git.checkout()
                    .setCreateBranch(true)
                    .setName(branch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setStartPoint(remote + "/" + branch)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Executes a checkout -b command using given branch.
     *
     * @param git    instance.
     * @param branch to create and checkout.
     * @return Ref to current branch.
     */
    public Ref createBranchAndCheckout(Git git, String branch) {
        try {
            return git.checkout()
                    .setCreateBranch(true)
                    .setName(branch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Add all files and commit them with given message. This is equivalent as doing git add . git commit -m "message".
     *
     * @param git     instance.
     * @param message of the commit.
     * @return RevCommit of this commit.
     */
    public RevCommit addAndCommit(Git git, String message) {
        try {
            git.add()
                    .addFilepattern(".")
                    .call();
            return git.commit()
                    .setMessage(message)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Add all files and commit them with given message. This is equivalent as doing git add . git commit -m "message".
     *
     * @param git     instance.
     * @param message of the commit.
     * @param author  of the commit.
     * @param email   of author of the commit.
     * @return RevCommit of this commit.
     */
    public RevCommit addAndCommit(Git git, String message, String author, String email) {
        try {
            git.add()
                    .addFilepattern(".")
                    .call();
            return git.commit()
                    .setAuthor(author, email)
                    .setMessage(message)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Pull repository from current branch and remote branch with same name as current
     *
     * @param git          instance.
     * @param remote       to be used.
     * @param remoteBranch to use.
     */
    public PullResult pullFromRepository(Git git, String remote, String remoteBranch) {
        try {
            return git.pull()
                    .setRemote(remote)
                    .setRemoteBranchName(remoteBranch)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Push all changes and tags to given remote.
     *
     * @param git    instance.
     * @param remote to be used.
     * @return List of all results of given push.
     */
    public Iterable<PushResult> pushToRepository(Git git, String remote) {
        try {
            return git.push()
                    .setRemote(remote)
                    .setPushAll()
                    .setPushTags()
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Push all changes and tags to given remote.
     *
     * @param git      instance.
     * @param remote   to be used.
     * @param username to login.
     * @param password to login.
     * @return List of all results of given push.
     */
    public Iterable<PushResult> pushToRepository(Git git, String remote, String username, String password) {
        try {
            return git.push()
                    .setRemote(remote)
                    .setPushAll()
                    .setPushTags()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a tag.
     *
     * @param git  instance.
     * @param name of the tag.
     * @return Ref created to tag.
     */
    public Ref createTag(Git git, String name) {
        try {
            return git.tag()
                    .setName(name)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Push all changes and tags to given remote.
     *
     * @param git        instance.
     * @param remote     to be used.
     * @param passphrase to access private key.
     * @param privateKey file location.
     * @return List of all results of given push.
     */
    public Iterable<PushResult> pushToRepository(Git git, String remote, String passphrase, Path privateKey) {
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setUserInfo(new PassphraseUserInfo(passphrase));
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                if (privateKey != null) {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(privateKey.toFile().getAbsolutePath());
                    return defaultJSch;
                } else {
                    return super.createDefaultJSch(fs);
                }
            }
        };

        try {
            return git.push()
                    .setRemote(remote)
                    .setPushAll()
                    .setPushTags()
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(sshSessionFactory);
                    })
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Pull repository from current branch and remote branch with same name as current
     *
     * @param git          instance.
     * @param remote       to be used.
     * @param remoteBranch to use.
     * @param username     to connect
     * @param password     to connect
     */
    public PullResult pullFromRepository(Git git, String remote, String remoteBranch, String username, String password) {
        try {
            return git.pull()
                    .setRemote(remote)
                    .setRemoteBranchName(remoteBranch)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Pull repository from current branch and remote branch with same name as current
     *
     * @param git          instance.
     * @param remote       to be used.
     * @param remoteBranch to use.
     * @param passphrase   to access private key.
     * @param privateKey   file location.
     */
    public PullResult pullFromRepository(final Git git, final String remote, String remoteBranch, final String passphrase, final Path privateKey) {
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setUserInfo(new PassphraseUserInfo(passphrase));
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                if (privateKey != null) {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(privateKey.toFile().getAbsolutePath());
                    return defaultJSch;
                } else {
                    return super.createDefaultJSch(fs);
                }
            }
        };

        try {
            return git.pull()
                    .setRemote(remote)
                    .setRemoteBranchName(remoteBranch)
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(sshSessionFactory);
                    })
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Clones a public remote git repository. Caller is responsible of closing git repository.
     *
     * @param remoteUrl to connect.
     * @param localPath where to clone the repo.
     * @return Git instance. Caller is responsible to close the connection.
     */
    public Git cloneRepository(String remoteUrl, Path localPath) {
        try {
            return Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(localPath.toFile())
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Clones a private remote git repository. Caller is responsible of closing git repository.
     *
     * @param remoteUrl to connect.
     * @param localPath where to clone the repo.
     * @param username  to connect
     * @param password  to connect
     * @return Git instance. Caller is responsible to close the connection.
     */
    public Git cloneRepository(String remoteUrl, Path localPath, String username, String password) {
        try {
            return Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .setDirectory(localPath.toFile())
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Clones a private remote git repository. Caller is responsible of closing git repository.
     *
     * @param remoteUrl  to connect.
     * @param localPath  where to clone the repo.
     * @param passphrase to access private key.
     * @param privateKey file location. If null default (~.ssh/id_rsa) location is used.
     * @return Git instance. Caller is responsible to close the connection.
     */
    public Git cloneRepository(final String remoteUrl, final Path localPath, final String passphrase, final Path privateKey) {

        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setUserInfo(new PassphraseUserInfo(passphrase));
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                if (privateKey != null) {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(privateKey.toFile().getAbsolutePath());
                    return defaultJSch;
                } else {
                    return super.createDefaultJSch(fs);
                }
            }
        };

        try {
            return Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(sshSessionFactory);
                    })
                    .setDirectory(localPath.toFile())
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checks if a repo has been cloned correctly.
     *
     * @param repo to check
     * @return true if has been cloned correctly, false otherwise
     */
    public boolean hasAtLeastOneReference(Repository repo) {

        for (Ref ref : repo.getAllRefs().values()) {
            if (ref.getObjectId() == null)
                continue;
            return true;
        }

        return false;
    }

}
