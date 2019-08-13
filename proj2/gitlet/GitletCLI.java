package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static gitlet.DiffType.del;

public class GitletCLI {
    /**
     * {@code gitlet add} command
     *
     * @param args The remaining arguments
     */
    public static void gitAdd(String[] args) {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        if (args.length == 0) {
            /* Wrong argument */
            System.out.println("Usage: java gitlet.Main add [file name]");
            return;
        } else if (args.length >= 1) {
            File destFile = new File(args[0]);
            if (!destFile.exists()) {
                /* File does not exist. */
                System.out.println("Files does not exist.");
                return;
            } else {
                /* File does exist. */
                FileDiff newFDiff = GitletWorkspace.generateFileDiff(destFile.getPath());
                if (newFDiff != null) {
                    /* Addition is successful */
                    StagedChanges sgArea = StagedChanges.load();
                    sgArea.add(destFile.getName(), newFDiff);
                    sgArea.save();
                } else {
                    /* Maybe not changed or something. generateFileDiff has dealt with it. */
                    return;
                }
            }
        }
    }

    /**
     * The git status command.
     */
    public static void gitStatus() {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        Map<String, String> fileList = GitletWorkspace.listFiles();
        StagedChanges sgArea = StagedChanges.load();
        Map<String, String> sgList = sgArea.getStagedList();

        /* A copy of the staging area.
         * Every time we find that a file is still in the working directory,
         * We remove it from the list. Otherwise a file is considered as deleted. */
        Map<String, String> copyList = new HashMap<>();
        copyList.putAll(sgList.entrySet().stream().parallel()
            .filter((e) -> !e.getValue().equals("-deleted-"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        copyList.putAll(GitletWorkspace.fileTracker.entrySet().stream().parallel()
            .filter((e) -> !e.getValue().equals("-deleted-"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        sgList.entrySet().stream().filter((e) -> e.getValue().equals("-deleted-")).forEach((e) -> {
            copyList.remove(e.getKey());
        });

        /** Tracked in the current commit, changed in the working directory, but not staged; or
         Staged for addition, but with different contents than in the working directory */
        Set<String> fileModified = new HashSet<>();
        Set<String> fileAdded = new HashSet<>();

        fileList.forEach((key, value) -> {
            /* The file is still in the working directory.
             *  Now: 1. If the file is also in the staging area, check for hash.
             *       2. Not in the staging area, then it's untracked. */
            copyList.remove(key);
            if (sgList.containsKey(key)) {
                /* This file is also in the staging area. Now check for hash. */
                if (!fileList.get(key).equals(sgList.get(key))) {
                    /* This file is different in the stating area and the working directory */
                    fileModified.add(key);
                }
            } else {
                if (GitletWorkspace.fileTracker.containsKey(key)
                    && !GitletWorkspace.fileTracker.get(key).equals("-deleted-")) {
                    /* Tracked in the current commit. */
                    if (!fileList.get(key).equals(GitletWorkspace.fileTracker.get(key))) {
                        /* changed in the working directory, but not staged */
                        fileModified.add(key);
                    }
                } else { /* This file is not in any previous commits. It's untracked. */
                    fileAdded.add(key);
                }
            }
        });

        System.out.println("=== Branches ===");
        Branch.getBranches().forEach(System.out::println);
        System.out.println();

        System.out.println("=== Staged Files ===");
        sgArea.changeList.entrySet().stream().sequential().filter((e) -> e.getValue().type != del)
            .map(Map.Entry::getKey).sorted().forEach((e) -> System.out.println(e));
        System.out.println();

        System.out.println("=== Removed Files ===");
        sgArea.changeList.entrySet().stream().sequential().filter((e) -> e.getValue().type == del)
            .map(Map.Entry::getKey).sorted().forEach((e) -> System.out.println(e));
        System.out.println();

        System.out.print("=== Modifications Not Staged For Commit ===");
        sortPrint(copyList.keySet(), "\n%s (deleted)");
        sortPrint(fileModified, "\n%s (modified)");
        System.out.println("\n"); //StyleChecker: Method length is 81 lines (max allowed is 80).

        System.out.println("=== Untracked Files ===");
        sortPrint(fileAdded, "%s\n");
        System.out.println();
    }

    public static void gitInit() {
        try {
            GitletWorkspace.load();
            System.out.println(
                "A gitlet version-control system already exists in the current directory.");
            return;
        } catch (NoGitException e) {
            GitletWorkspace.init();

            /* Create an empty staging area. */
            StagedChanges sgArea = new StagedChanges();
            sgArea.save();

            /* Create new commit. */
            Commit newCommit = new Commit(sgArea, "initial commit");

            /* Load the current branch */
            Branch currBranch = Branch.load(GitletWorkspace.branchName);

            /* Add commit. This method will also save the commit. */
            currBranch.addCommit(newCommit);
        }
    }

    public static void gitCommit(String[] args) {

        /* Initialize git repository. */
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        /* Get the commit message */
        String msg = String.join(" ", args).strip();
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        /* Load the staging area. */
        StagedChanges sgArea = StagedChanges.load();
        if (sgArea == null || sgArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        /* Create new commit. */
        Commit newCommit = new Commit(sgArea, msg);

        /* Load the current branch */
        Branch currBranch = Branch.load(GitletWorkspace.branchName);

        /* Add commit. This method will also save the commit. */
        currBranch.addCommit(newCommit);

        /* Reload the file tracker. */
        GitletWorkspace.fileTracker = newCommit.listOfFiles.entrySet()
            .stream().parallel().collect(Collectors.toMap(Map.Entry::getKey,
                (e) -> e.getValue().hashCodeString()));
        GitletWorkspace.saveTracker();

        /* Clear the staging area. */
        new StagedChanges().save();
    }

    public static void gitLog() {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        Branch currBranch = Branch.load(GitletWorkspace.branchName);
        currBranch.printLog();
    }

    public static void gitGlobalLog() {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        Branch currBranch = Branch.load(GitletWorkspace.branchName);
        currBranch.printGlobalLog();
    }

    public static void gitFind(String[] toFind) {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        if (toFind.length != 1) {
            System.out.println("Incorrect operands.");
        } else {
            Branch currBranch = Branch.load(GitletWorkspace.branchName);
            currBranch.find(toFind[0]);
        }
    }

    public static void gitCheckout(String[] args) {
        if (args.length == 0) {
            System.out.println("Incorrect operands.");
            return;
        }

        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }
        Branch currBranch = Branch.load(GitletWorkspace.branchName);
        if (currBranch == null) {
            return;
        }

        if (args.length == 1) {
            /* Recognized as branch switch. */
            if (Branch.getBranchesNoAsterisk().contains(args[0])) {
                /* Recognized as branches. */
                if (GitletWorkspace.branchName.equals(args[0])) {
                    System.out.println("No need to checkout the current branch.");
                }
                Branch toBranch = Branch.load(args[0]);
                if (toBranch == null) {
                    System.out.println("No such branch exists."); // Should not happen.
                } else {
                    toBranch.checkoutFrom(currBranch);
                }
            } else {
                System.out.println("No such branch exists.");
            }
        } else if (args.length == 2) {
            /* Recognized as HEAD checkout. */
            if (!args[0].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            Commit headCommit = Commit.load(currBranch.HEAD);
            if (!headCommit.contains(args[1])) {
                System.out.println("File does not exist in that commit");
            } else {
                headCommit.checkout(args[1]);
            }
            return;
        } else if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            /* Recognized as checking out to a commit. */
            /* args[0]: Commit hash, args[1]: "--", args[2]: filename*/
            if (currBranch.containsCommit(args[0])) {
                Commit wantedCommit = Commit.load(currBranch.getFullCommit(args[0]));
                if (wantedCommit == null) {
                    System.out.println("No commit with that id exists. -WEIRD");
                    return;
                }
                if (wantedCommit.contains(args[2])) {
                    wantedCommit.checkout(args[2]);
                } else {
                    System.out.println("File does not exist in that commit.");
                }
            } else {
                System.out.println("No commit with that id exists.");
            }
        }
    }

    public static void gitRm(String[] args) {
        if (args.length == 0) {
            System.out.println("Incorrect operands.");
            return;
        }

        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        StagedChanges sgArea = StagedChanges.load();
        String fileName = args[0];
        File currFile = new File(fileName);

        if (GitletWorkspace.fileTracker.containsKey(fileName)
            && !GitletWorkspace.fileTracker.get(fileName).equals("-deleted-")) {
            /* The file is tracked by the current commit. */

            /* Remove it in the filetracker.
             * So it will become untracked, and will not be included in the next commit. */
            // GitletWorkspace.fileTracker.remove(fileName);
            // GitletWorkspace.saveTracker();

            /* Delete it from the working directory. */
            Utils.restrictedDelete(currFile);

            /* Remove it from the staging area. In my case, mark as deleted. */
            FileDiff tmpDiff = new FileDiff(DiffType.del, null);
            sgArea.changeList.put(fileName, tmpDiff);
            sgArea.save();
        } else if (sgArea.changeList.containsKey(fileName)) {
            /* Unstage the file and do nothing else. */
            sgArea.remove(fileName);
            sgArea.save();
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public static void gitBranch(String[] args) {
        /* Initialize git repository. */
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        if (args.length == 0) {
            System.out.println("Incorrect operands.");
        } else {
            Branch currBranch = Branch.load(GitletWorkspace.branchName);
            Branch newBranch = currBranch.branchTo(args[0]);
            if (newBranch != null) {
                newBranch.save();
            }
        }

    }

    public static void gitRmBranch(String[] args) {
        /* Initialize git repository. */
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        if (args.length == 0) {
            System.out.println("Incorrect operands.");
        }
        if (!Branch.getBranchesNoAsterisk().contains(args[0])) {
            System.out.println("A branch with that name does not exist.");
        } else if (GitletWorkspace.branchName.equals(args[0])) {
            System.out.println("Cannot remove the current branch.");
        } else {
            Branch.rmBranch(args[0]);
        }
    }

    public static void gitReset(String[] args) {
        /* Initialize git repository. */
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        if (args.length == 0) {
            System.out.println("Incorrect operands.");
            return;
        } else {
            Branch currBranch = Branch.load(GitletWorkspace.branchName);
            Set<String> untracked = GitletWorkspace.getUntrackedFiles();

            if (currBranch.containsCommit(args[0])) {
                Commit toCommit = currBranch.getFullCommitCommit(args[0]);
                Commit fromCommit = currBranch.getFullCommitCommit(currBranch.HEAD);

                Set<String> overFiles = toCommit.listFilesIncluded();
                untracked.retainAll(overFiles);
                if (untracked.size() != 0) {
                    System.out.println(
                        "There is an untracked file in the way; delete it or add it first.");
                    return;
                } else {
                    fromCommit.undo();
                    toCommit.checkout();
                    currBranch.HEAD = currBranch.getFullCommit(args[0]);
                    /* Reset the file tracker. */
                    GitletWorkspace.loadTrackerFromCommit(toCommit);
                    currBranch.save();
                    /* Reset the staging area. */
                    new StagedChanges().save();
                }
            } else {
                /* Check if the that commit is on another branch. */
                List<Branch> branchList = Branch.getBranchesNoAsterisk().stream().parallel()
                    .filter((s) -> !s.equals(GitletWorkspace.branchName))
                    .map(Branch::load).collect(Collectors.toList());
                for (Branch pBranch : branchList) {
                    if (pBranch.containsCommit(args[0])) {
                        Commit toCommit = pBranch.getFullCommitCommit(args[0]);
                        Commit fromCommit = currBranch.getFullCommitCommit(currBranch.HEAD);
                        Set<String> overFiles = toCommit.listFilesIncluded();
                        untracked.retainAll(overFiles);
                        if (untracked.size() != 0) {
                            System.out.println(
                                "There is an untracked file in the way; "
                                    + "delete it or add it first.");
                            return;
                        } else {
                            fromCommit.undo();
                            toCommit.checkout();
                            /* Switch branch. */
                            GitletWorkspace.branchName = pBranch.branchName;
                            GitletWorkspace.saveConfig();
                            pBranch.HEAD = toCommit.hashCodeString();
                            pBranch.save();
                            /* Reset the file tracker. */
                            GitletWorkspace.loadTrackerFromCommit(toCommit);
                            /* Reset the staging area. */
                            new StagedChanges().save();
                        }
                        return;
                    }
                }
                System.out.println("No commit with that id exists.");
            }
        }
    }

    public static void gitMerge(String[] args) {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }
        if (args.length == 0) {
            System.out.println("Incorrect operands.");
            return;
        }
        /* Check the staging area. */
        StagedChanges sgArea = StagedChanges.load();
        if (!sgArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        /* Ensure it's not the current branch */
        if (args[0].equals(GitletWorkspace.branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        /* Check the branch exists. */
        if (!Branch.getBranchesNoAsterisk().contains(args[0])) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        /* Start working. */
        Branch currBranch = Branch.load(GitletWorkspace.branchName);
        Branch srcBranch = Branch.load(args[0]);
        currBranch.mergeFrom(srcBranch);
    }

    /**
     * Sort a String collection, then print.
     *
     * @param toPrint The collection to print
     * @param format  Formatter.
     */
    private static void sortPrint(Collection<String> toPrint, String format) {
        List<String> tmpList = new ArrayList<>(toPrint);
        Collections.sort(tmpList);
        tmpList.forEach((s) -> System.out.print(String.format(format, s)));
    }
}
