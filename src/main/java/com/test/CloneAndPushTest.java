package com.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * @author ryan
 * @date 19-9-19 下午3:37
 */
public class CloneAndPushTest {

    public static void main(String[] args) throws IOException, GitAPIException {
        testCloneFromRemote();
    }


    private static final String REMOTE_URL = "https://github.com/Ryan-Miao/test-dag.git";

    public static void testCloneFromRemote() throws IOException, GitAPIException {
        // prepare a new folder for the cloned repository
        File localPath = File.createTempFile("TestGitRepository", "");
        if (!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);

        CredentialsProvider usernamePasswordCredentialsProvider =
            new UsernamePasswordCredentialsProvider("your username", "yourpass");

        Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setCredentialsProvider(usernamePasswordCredentialsProvider)
            .setDirectory(localPath)
            .setProgressMonitor(new SimpleProgressMonitor())
            .call();
        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
        System.out.println("Having repository: " + git.getRepository().getDirectory());

        File myFile = new File(git.getRepository().getDirectory().getParent(), "testfile");
        if (!myFile.createNewFile()) {
            throw new IOException("Could not create file " + myFile);
        }

        // write file
        FileWriter writer = new FileWriter(myFile);
        writer.write("ryan.miao");
        writer.flush();
        writer.close();

        // run the add-call
        git.add()
            .addFilepattern("testfile")
            .call();

        git.commit()
            .setMessage("init")
            .call();

        Repository repository = git.getRepository();
        git.push().setCredentialsProvider(usernamePasswordCredentialsProvider)
            .call();

        // clean up here to not keep using more and more disk-space for these samples
        FileUtils.deleteDirectory(localPath);
    }

    private static class SimpleProgressMonitor implements ProgressMonitor {

        @Override
        public void start(int totalTasks) {
            System.out.println("Starting work on " + totalTasks + " tasks");
        }

        @Override
        public void beginTask(String title, int totalWork) {
            System.out.println("Start " + title + ": " + totalWork);
        }

        @Override
        public void update(int completed) {
            System.out.print(completed + "-");
        }

        @Override
        public void endTask() {
            System.out.println("Done");
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
