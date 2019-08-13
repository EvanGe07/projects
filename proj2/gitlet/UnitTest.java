package gitlet;

import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/* The suite of all JUnit tests for the gitlet package.
   @author
 */
public class UnitTest {

    @Test
    public void placeholderTest() {
    }

    @Test
    public void lookUpCommits() {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            e.printStackTrace();
            return;
        }

        Branch currBranch = Branch.load(GitletWorkspace.branchName);

        List<Commit> allCommits = currBranch.commitList.stream()
            .map(Commit::load).collect(Collectors.toList());

        return;
    }

    @Test
    public void testFileBehavior() {
        File testFile = new File(".\\test1.txt");
        System.out.println(testFile.getPath());
        System.out.println(testFile.getName());
    }

    @Test
    public void initrepstuff() {
        Main.main("init");
        Main.main("add", "test1.txt");
        Main.main("add", "test2.txt");
        Main.main("commit", "Ver 1s of files");
        Main.main("branch", "other");
    }
}
