package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Branch implements Serializable {
    String branchName;

    List<String> commitList;

    /**
     * Indicates that the current branch is in merging mode.
     * Another branch is being merged into this branch.
     */
    boolean mergingMode;

    /**
     * When in merging mode, this indicates the other branch.
     */
    String mergingModeBranchname;

    /* Remembers each branching and merging.
     * Stores the last common commit with another branch */
    Map<String, String> branchingHistory;

    String HEAD;

    /**
     * Create a new branch, with empty list of commits.
     *
     * @param name Name of the branch to create.
     */
    Branch(String name) {
        branchName = name;
        commitList = new ArrayList<>();
        branchingHistory = new HashMap<>();
        mergingMode = false;
        mergingModeBranchname = "";
    }

    public String getBranchName() {
        return branchName;
    }

    /**
     * Add a commit to the current branch. Can be a MergingCommit.
     * This will save the commit to commits directory.
     *
     * @param c The commit to add.
     */
    public void addCommit(Commit c) {
        String commitHex = c.hashCodeString();
//      String cFileName = GitletWorkspace.getGitCommitsDir() + File.separator + commitHex + ".obj";

        /* If the commit is not written to disk, write it. */
//        File commitFile = new File(cFileName);
//        if (!commitFile.exists()) {
//            try {
//                commitFile.createNewFile();
//                FileOutputStream fout = new FileOutputStream(commitFile);
//                ObjectOutputStream oout = new ObjectOutputStream(fout);
//                oout.writeObject(c);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        c.save();

        /* Add the commit to the current branch list. */
        commitList.add(c.hashCodeString());
        HEAD = c.hashCodeString();
        save();

        if (mergingMode) {
            mergingMode = false;
            this.branchingHistory.put(mergingModeBranchname, c.hashCodeString());
            Branch other = Branch.load(mergingModeBranchname);
            other.branchingHistory.put(branchName, other.HEAD);
            mergingModeBranchname = "";
            other.save();
            save();
        }
    }


    /**
     * Branch OUT from the current branch.
     * <p>
     * Create a branch from HEAD of the current branch.<br>
     * Commit list and merging history will also be copied.
     * </p>
     *
     * @param name The name of the new branch.
     * @return A new branch created.
     */
    public Branch branchTo(String name) {
        if (Branch.getBranchesNoAsterisk().contains(name)) {
            System.out.println("A branch with that name already exists.");
            return null;
        }
        Branch newBranch = new Branch(name);
        newBranch.commitList = new ArrayList<>(commitList);
        newBranch.branchingHistory = new HashMap<>(branchingHistory);
        newBranch.HEAD = this.HEAD;
        newBranch.branchingHistory.put(this.branchName, HEAD);
        this.branchingHistory.put(name, HEAD);
        save();
        return newBranch;
    }

    /**
     * Fast-forward this branch to another branch.
     *
     * @param otherBranch Another branch that's ahead of this branch.
     */
    private void doFastForward(Branch otherBranch) {
        HEAD = otherBranch.HEAD;
        GitletWorkspace.branchName = this.branchName;
        this.commitList = otherBranch.commitList;
        GitletWorkspace.saveConfig();
        GitletWorkspace.loadTrackerFromCommit(Commit.load(HEAD));
        System.out.println("Current branch fast-forwarded.");
    }

    /**
     * Check if there're any unstaged files that will be overwritten
     * if we revert to {@code toCommit}
     *
     * @param toCommit The commit we're trying to checkout.
     * @return True if There's no untracked files that will be overwritten.
     */
    private boolean checkUnstagedFiles(Commit toCommit) {
        Set<String> untrackedChanges = GitletWorkspace.getUntrackedFiles();
        untrackedChanges.retainAll(toCommit.listFilesIncluded());
        if (untrackedChanges.size() != 0) {
            System.out.println("There is an untracked file in the way; delete it or add it first.");
            return false;
        }
        return true;
    }

    /**
     * Load FileDiff from hashes, create a merging mark, and write it to filename.
     * Will not add it to the staging area.
     *
     * @param hashSource Hashcode of FileDiff(the file) from "given branch"
     * @param hashMine   Hashcode of FileDiff(the file) from "current branch"
     * @param filename   The file to write to.
     */
    private void doMergeMark(String hashSource, String hashMine, String filename) {
        String srcCont = new String(FileDiff.load(hashSource).fileStore);
        String myCont = new String(FileDiff.load(hashMine).fileStore);
        String res = String.format("<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n", myCont, srcCont);
        Utils.writeContents(new File(filename), res.getBytes());
    }

    /**
     * Load FileDiff from hashes, create a merging mark, and write it to filename.
     * Will not add it to the staging area.
     * Accepts null value as hashes. Will be replaced with empty content.
     *
     * @param hashSource Hashcode of FileDiff(the file) from "given branch"
     * @param hashMine   Hashcode of FileDiff(the file) from "current branch"
     * @param filename   The file to write to.
     */
    private void doMergeMarkNull(String hashSource, String hashMine, String filename) {
        String srcCont = hashSource == null ? "" : new String(FileDiff.load(hashSource).fileStore);
        String myCont = hashMine == null ? "" : new String(FileDiff.load(hashMine).fileStore);
        String res = String.format("<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n", myCont, srcCont);
        Utils.writeContents(new File(filename), res.getBytes());
    }

    /**
     * Generate a FileDiff of type add. Helper method for merge to prevent it from getting too long.
     *
     * @param hashSource Hashcode of source file.
     * @param filename   Name of file.
     * @return A filediff with content in hashSource, and type add.
     */
    private FileDiff genAddDiff(String hashSource, String filename) {
        FileDiff tmpDiff = FileDiff.load(hashSource);
        tmpDiff.checkout(new File(filename));
        tmpDiff.type = DiffType.add;
        return tmpDiff;
    }

    /**
     * Generate a FileDiff of type mod. Helper method for merge to prevent it from getting too long.
     *
     * @param hashSource Hashcode of source file.
     * @param filename   Name of file.
     * @return A filediff with content in hashSource, and type mod.
     */
    private FileDiff genModDiff(String hashSource, String filename) {
        FileDiff tmpDiff = FileDiff.load(hashSource);
        tmpDiff.checkout(new File(filename));
        tmpDiff.type = DiffType.mod;
        return tmpDiff;
    }

    /**
     * Merge another branch into this branch.
     *
     * @param otherBranch The source branch.
     */
    public void mergeFrom(Branch otherBranch) {
        String otherHEAD = otherBranch.HEAD;
        if (commitList.contains(otherHEAD)) {
            System.out.println("Given branch is an ancestor of the current branch.");
        } else if (otherBranch.commitList.contains(HEAD)) {
            if (otherBranch.checkoutFrom(this)) {
                doFastForward(otherBranch);
            }
        } else { /* Normal operation. */
            Commit toCommit = Commit.load(otherBranch.HEAD);
            Commit fromCommit = Commit.load(HEAD);
            Commit myCommonCommit = Commit.load(branchingHistory.get(otherBranch.branchName));
            if (!checkUnstagedFiles(toCommit)) {
                return;
            }
            StagedChanges sgArea = new StagedChanges();
            boolean noConflict = true;
            mergingMode = true;
            mergingModeBranchname = otherBranch.branchName;
            for (Map.Entry<String, Commit.FileRecord> e : toCommit.listOfFiles.entrySet()) {
                String filename = e.getKey();
                if (myCommonCommit.contains(filename)) { /* File existed at split point */
                    String hashMine = fromCommit.listOfFiles.get(filename).fileDiff;
                    String hashSource = e.getValue().fileDiff;
                    String hashCommon = myCommonCommit.listOfFiles.get(filename).fileDiff;
                    if (hashSource.equals("-deleted-")) { /* Removed in the source(given) branch */
                        if (hashCommon.equals(hashMine)) { /* Unchanged in the current branch. RM */
                            Utils.restrictedDelete(filename);
                            sgArea.add(filename, new FileDiff(DiffType.del, null));
                        } else { /* Changed in the current branch. Conflict */
                            noConflict = false;
                            doMergeMarkNull(null, hashMine, filename);
                        }
                    } else {
                        /* File existed at split point and in the source branch. */
                        if (fromCommit.contains(filename)) {
                            /* The file is also in the current branch. Now check fo hash */
                            if (hashCommon.equals(hashMine) && !hashCommon.equals(hashSource)) {
                                /* Not changed in the current branch, but changed in source bnc. */
                                sgArea.add(filename, genModDiff(hashSource, filename));
                            } else if (!hashCommon.equals(hashMine)
                                && !hashCommon.equals(hashSource) && !hashSource.equals(hashMine)) {
                                /* Changed in both branches. Create a merge mark. */
                                noConflict = false;
                                doMergeMark(hashSource, hashMine, filename);
                            }
                        } else if (!hashCommon.equals(hashSource)) {
                            /* Changed in the given branch, but absent in the current branch. */
                            doMergeMarkNull(hashSource, null, filename);
                        }
                    }
                } else { /* Not present at the split point */
                    String hashSource = e.getValue().fileDiff;
                    if (fromCommit.contains(filename)) { /* Also created in my branch. */
                        String hashMine = fromCommit.listOfFiles.get(filename).fileDiff;
                        if (!hashMine.equals(hashSource)) { /* Added differently. */
                            noConflict = false;
                            doMergeMark(hashSource, hashMine, filename);
                        }
                    } else { /* Not in current branch. */
                        sgArea.add(filename, genAddDiff(hashSource, filename));
                    }
                }
            }
            sgArea.save();
            if (noConflict) {
                Commit megCommit = new Commit(sgArea, "Merged "
                    + branchName + " with " + otherBranch.branchName + ".");
                megCommit.save();
                GitletWorkspace.loadTrackerFromCommit(megCommit);
                addCommit(megCommit);
                new StagedChanges().save();
            } else {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }

    /**
     * Checkout from another branch to this branch.
     * This method will remove all files involved in another branch
     * And write all files involved in this branch.
     *
     * @param otherBranch The main branch before this checkout.
     * @return True if successful.
     */
    public boolean checkoutFrom(Branch otherBranch) {
        /* Get the head commit of the other branch. */
        Commit otherHEAD = Commit.load(otherBranch.HEAD);
        Commit myHEAD = Commit.load(this.HEAD);

        Set<String> untracked = GitletWorkspace.getUntrackedFiles();
        Set<String> myCommitFiles = myHEAD.listOfFiles.keySet();

        untracked.retainAll(myCommitFiles);
        if (untracked.size() != 0) {
            System.out.println("There is an untracked file in the way; delete it or add it first.");
            return false;
        }

        /* Remove all files involved in the other branch. */
        otherHEAD.listOfFiles.forEach((key, value) -> {
            if (value.type != DiffType.del) {
                File curr = new File(key);
                curr.delete();
            }
        });

        /* Write files involved in the head of this branch. */
        boolean flagSuccess = true;
        for (Map.Entry<String, Commit.FileRecord> entry : myHEAD.listOfFiles.entrySet()) {
            String key = entry.getKey();
            Commit.FileRecord value = entry.getValue();
            if (value.type != DiffType.del && !value.fileDiff.equals("-deleted-")) {
                try {
                    File curr = new File(key);
                    curr.createNewFile();
                    FileDiff myFile = FileDiff.load(value.fileDiff);
                    Utils.writeContents(curr, myFile.fileStore);
                } catch (IOException e) {
                    e.printStackTrace();
                    flagSuccess = false;
                }
            }
        }

        /* Switch branch. */
        GitletWorkspace.branchName = this.branchName;
        GitletWorkspace.saveConfig();

        /* Clear the staging area. */
        new StagedChanges().save();

        /* Reset the file tracker to the head of current commit. */
        GitletWorkspace.fileTracker =
            myHEAD.listOfFiles.entrySet().stream().parallel()
                .collect(Collectors.toMap(Map.Entry::getKey,
                    (e) -> e.getValue().fileDiff));
        GitletWorkspace.saveTracker();

        return flagSuccess;
    }

    /**
     * Get a set of files changed since commit {@code commitId}
     *
     * @param commitId ID of the commit to look for, exclusive
     * @return A set of relative paths to the file.
     */
    private Set<String> getChangedSince(int commitId) {
        int lastIndex = commitList.lastIndexOf(commitId);
        if (lastIndex == -1) {
            return null;
        } else {
            Set<String> fileChanged = new HashSet<>();
            for (int i = lastIndex + 1; i < commitList.size(); i++) {
                // TODO fileChanged.addAll(commitList.get(i))
                break;
            }
        }
        return null;
    }

    /**
     * Revert the working directory to the commit specified.
     *
     * @param commitHash The hash of the commit to revert to
     */
    public void revert(int commitHash) {
        int indexToRev = commitList.lastIndexOf(commitHash);
        Map<String, FileDiff> diffCollect;
        for (int i = commitList.size() - 1; i > indexToRev; i -= 1) {
            String currCommHash = commitList.get(i);
            Commit currComm = Commit.load(currCommHash);
            if (currComm == null) {
                System.out.println("Cannot find commit durng collect: " + currCommHash);
            }
            // TODO Some problems here!!
        }
    }

    private static String ripToBranchName(String k) {
        int indexOfSlash = k.lastIndexOf(File.separator);
        int indexOfPoint = k.lastIndexOf(".") == -1 ? k.length() : k.lastIndexOf(".");
        return k.substring(indexOfSlash + 1, indexOfPoint);
    }

    /**
     * Get a list of branches. The current branch will be marked with an asterisk (*).
     *
     * @return A set of branches
     */
    public static List<String> getBranches() {
        return Utils.plainFilenamesIn(GitletWorkspace.getGitBranchesDir()).stream()
            .parallel()
            .map(String::strip)
            .map(Branch::ripToBranchName)
            .sorted()
            .map((s) -> s.equals(GitletWorkspace.getBranchName()) ? "*" + s : s)
            .collect(Collectors.toList());
    }

    /**
     * Get a list of branches
     *
     * @return A set of branches
     */
    public static Set<String> getBranchesNoAsterisk() {
        return Utils.plainFilenamesIn(GitletWorkspace.getGitBranchesDir()).stream()
            .parallel()
            .map(String::strip)
            .map(Branch::ripToBranchName)
            .collect(Collectors.toSet());
    }

    /**
     * Save the current branch to the default location.
     */
    public void save() {
        String brDir = GitletWorkspace.getGitBranchesDir() + File.separator;
        File myPath = new File(brDir + branchName + ".obj");
        if (!myPath.exists()) {
            try {
                myPath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fout = new FileOutputStream(myPath);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(this);
            oout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load a branch from file.
     */
    public static Branch load(String name) {
        String brDir = GitletWorkspace.getGitBranchesDir() + File.separator;
        File myPath = new File(brDir + name + ".obj");
        if (myPath.exists()) {
            try {
                FileInputStream fin = new FileInputStream(myPath);
                ObjectInputStream oin = new ObjectInputStream(fin);
                Branch getBr = (Branch) oin.readObject();
                return getBr;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return null;
    }

    /**
     * Print the log of this branch in specified format, starting from HEAD.
     */
    public void printLog() {
        int indexOfHead = commitList.lastIndexOf(HEAD);
        for (int i = indexOfHead; i >= 0; i--) {
            String s = commitList.get(i);

            System.out.println("===");
            System.out.println("Commit " + s);

            /* Load the commit. */
            Commit curr = Commit.load(s);
            DateTimeFormatter dtformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println(curr.getTimestamp().format(dtformat));
            System.out.println(curr.getMessage());
            System.out.println();
        }
    }

    /**
     * Print the full log of this branch, no matter where HEAD is.
     */
    public void printGlobalLog() {
        for (int i = commitList.size() - 1; i >= 0; i--) {
            String s = commitList.get(i);

            System.out.println("===");
            System.out.println("Commit " + s);

            /* Load the commit. */
            Commit curr = Commit.load(s);
            DateTimeFormatter dtformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println(curr.getTimestamp().format(dtformat));
            System.out.println(curr.getMessage());
            System.out.println();
        }
    }

    /**
     * Find commits with commit message
     */
    public void find(String msg) {
        List<String> tmpList = commitList.stream()
            .parallel()
            .map(Commit::load)
            .filter(Objects::nonNull)
            .filter((c) -> c.getMessage().contains(msg))
            .map(Commit::hashCodeString)
            .collect(Collectors.toList());

        if (tmpList.size() == 0) {
            System.out.println("Found no commit with that message.");
        } else {
            tmpList.forEach(System.out::println);
        }
    }

    /**
     * Traverse the branch, and find the latest record of a file.
     *
     * @param filename The filename to look for
     * @return The content of file.
     */
    public FileDiff getLatestVersion(String filename) {
        for (int i = commitList.lastIndexOf(HEAD); i >= 0; i -= 1) {
            Commit currCommit = Commit.load(commitList.get(i));
            if (currCommit.contains(filename)) {
                return FileDiff.load(currCommit.listOfFiles.get(filename).hashCodeString());
            }
        }
        return null;
    }

    public boolean containsCommit(String partialHex) {
        for (String s : commitList) {
            if (s.contains(partialHex)) {
                return true;
            }
        }
        return false;
    }

    public String getFullCommit(String partialHex) {
        for (String s : commitList) {
            if (s.contains(partialHex)) {
                return s;
            }
        }
        return "";
    }

    public Commit getFullCommitCommit(String partialHex) {
        for (String s : commitList) {
            if (s.contains(partialHex)) {
                return Commit.load(s);
            }
        }
        return null;
    }

    /**
     * Remove a branch from the file system.
     *
     * @param branchName Name of the branch to remove.
     */
    public static void rmBranch(String branchName) {
        String brDir = GitletWorkspace.getGitBranchesDir() + File.separator;
        File myPath = new File(brDir + branchName + ".obj");
        if (myPath.exists()) {
            myPath.delete();
        }
    }
}
