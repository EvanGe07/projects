package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class represents a snapshot of files, finalized.
 * <p>
 * Each commit will be stored as a separate file, as {@code .gitlet/commits/{$hash}.obj}<br>
 * A commit should not be created explicitly with parameters.
 * Instead, a commit should be created with {@code StagedChanges}.
 */
public class Commit implements Serializable {

    /**
     * Refactor - All files.
     * First String: filename.
     * Second string: corresponding hash of FileDiff.
     */
    protected Map<String, FileRecord> listOfFiles;

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Time of this commit being made
     */
    private ZonedDateTime timestamp;

    /**
     * Name of person who made the commit.
     */
    private String name;

    /**
     * email of person who made the commit.
     */
    private String email;

    /**
     * Hashcode of this commit
     */
    private String hashCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    /**
     * Initialize a commit with nothing.
     * This should only be used by MergingCommit
     */
    protected Commit() {
        listOfFiles = null;
    }

    public class FileRecord implements Serializable {
        DiffType type;
        String fileDiff;

        public FileRecord(FileDiff copyFromDiff) {
            this.fileDiff = copyFromDiff.hashCodeString();
            this.type = copyFromDiff.type;
        }

        public FileRecord(String fileHash, DiffType changeType) {
            this.fileDiff = fileHash;
            this.type = changeType;
        }

        public String hashCodeString() {
            if (fileDiff.equals("-deleted-")) {
                return "-deleted-";
            }
            return FileDiff.load(fileDiff).hashCodeString();
        }
    }

    /**
     * Initialize a commit with the current staging area.
     *
     * @param c The Staging area.
     */
    Commit(StagedChanges c) {
        /* First, load the filetracker(last commit) */
        /* Everything in the last commit will be seen as "same". */
        this.listOfFiles = new HashMap<>();
        GitletWorkspace.fileTracker.forEach((key, value) -> {
            DiffType changeType = DiffType.same;
            this.listOfFiles.put(key, new FileRecord(value, changeType));
        });

        /* For each file in the staging area, replace them. */
        for (Map.Entry<String, FileDiff> e : c.changeList.entrySet()) {
            String key = e.getKey();
            FileDiff value = e.getValue();
            this.listOfFiles.put(key, new FileRecord(value));
            value.save();
        }

        this.name = GitletWorkspace.getName();
        this.email = GitletWorkspace.getEmail();
        this.timestamp = ZonedDateTime.now();
        recalcHash();
    }

    Commit(StagedChanges c, String msg) {
        this(c);
        this.message = msg;
        recalcHash();
    }

    /**
     * Save this commit to a file, {@code .gitlet/commits/{$hash}.obj}
     */
    public void save() {
        File outFile = getCommitFile(hashCode);
        try {
            outFile.createNewFile();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(this);
            out.close();
        } catch (IOException excp) {
            excp.printStackTrace();
        }
    }

    /**
     * Load and returns a commit from disk
     *
     * @param commitId The ID of the commit, in a string format.
     *                 This string should be given by Utils.sha1()
     * @return
     */
    public static Commit load(String commitId) {
        if (commitId == null) {
            return null;
        }
        File inFile = getCommitFile(commitId);
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            Commit loadedCommit = (Commit) inp.readObject();
            inp.close();
            return loadedCommit;
        } catch (IOException | ClassNotFoundException excp) {
            excp.printStackTrace();
            return null;
        }
    }

    /**
     * <em>Restore all files involved in this commit</em><br><br>
     * <p>
     * Write the working directory. Restores all the file involved in this commit
     * to the status prior to this commit.<br>
     * If {@code type == add}, the file will be deleted.
     * </p>
     * This could happen either during revert, or during forwarding.
     */
    public void checkout() {
        // iterate through the map
        listOfFiles.forEach((file, record) -> {
            if (record.type != DiffType.del && !record.fileDiff.equals("-deleted-")) {
                try {
                    File tmpFile = new File(file);
                    tmpFile.createNewFile();
                    FileDiff savedContent = FileDiff.load(record.fileDiff);
                    Utils.writeContents(tmpFile, savedContent.fileStore);
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            } else {
                File tmpFile = new File(file);
                Utils.restrictedDelete(tmpFile);
            }
        });
    }


    public boolean checkout(String filename) {
        return checkout(filename, new File(filename));
    }

    /**
     * <em>Restore specified file in this commit</em><br><br>
     * <p>
     * Writes the working directory. If {@code filename} is a part of this commit,
     * restore it to the stored state, then return true.
     * Otherwise, return false
     *
     * @param filename    The path of file to look for.
     * @param fileToCheck The path to write to. In most cases it should equal filename.
     * @return True if file exists in this commit and operation successful.
     */
    public boolean checkout(String filename, File fileToCheck) {
        FileDiff file = FileDiff.load(listOfFiles.get(filename).hashCodeString());
        /* commDiffType is the type of file differ specified to this commit. */
        DiffType commDiffType = listOfFiles.get(filename).type;
        if (commDiffType == DiffType.del) {
            if (fileToCheck.exists()) {
                fileToCheck.delete();
                return true;
            }
            return false;
        }

        if (!fileToCheck.exists()) {
            try {
                fileToCheck.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try (FileOutputStream fout = new FileOutputStream(fileToCheck)) {
            fout.write(file.fileStore);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Check if the file specified by relative path {@code filename} is involved in this commit.
     *
     * @param filename Relative path of file.
     * @return True if filename is a part of this commit.
     */
    public boolean contains(String filename) {
        return listOfFiles.containsKey(filename)
            && !listOfFiles.get(filename).fileDiff.equals("-deleted-");
    }

    /**
     * Return a list of files involved in this commit.
     * This is usually used when collecting a set of file changes.
     *
     * @return A map. Keys are the files changed, values are the type of change.
     */
    public Map<String, DiffType> fileList() {
        Map<String, DiffType> chList = new HashMap<>();
        listOfFiles.entrySet().forEach((e) -> {
                FileDiff load = FileDiff.load(e.getValue().hashCodeString());
                if (load != null) {
                    chList.put(e.getKey(), e.getValue().type);
                }
            }
        );
        return chList;
    }

    /**
     * Get the Integer hashcode.
     *
     * @return The decimal form of the hashcode.
     */
    @Override
    @Deprecated
    public int hashCode() {
        return Integer.parseInt(hashCode, 16);
    }

    /**
     * See if two commits are equal. This is rarely used.
     *
     * @param other The other commit
     * @return True if equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Commit) {
            return this.hashCodeString().equals(((Commit) other).hashCodeString());
        } else {
            return false;
        }
    }

    public String hashCodeString() {
        return hashCode;
    }

    /**
     * Recauclates the hashcode based on the contents in this commit.
     * Internal helper method.
     */
    private void recalcHash() {
        try {
            ByteArrayOutputStream baostream = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(baostream);
            oout.writeObject(this);
            oout.close();
            hashCode = Utils.sha1(baostream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given hashcode, get the desired location of the commit being stored.
     *
     * @param hash A hash code in hex string.
     * @return File pointing to the commit. Does not guarantee existence.
     */
    private static File getCommitFile(String hash) {
        return new File(
            GitletWorkspace.getGitCommitsDir()
                + File.separator
                + hash + ".obj"
        );
    }

    /**
     * Remove all files in this commit.
     */
    public void undo() {
        StagedChanges sgArea = StagedChanges.load();

        /* Remove all files recorded in this commit. */
        listOfFiles.forEach((fileName, record) -> {
            File currFile = new File(fileName);
            if (currFile.exists()) {
                Utils.restrictedDelete(currFile);
            }
        });

        /* Clear the staging area. */
        new StagedChanges().save();
    }

    /**
     * List all files included in this commit, not including those marked as removed.
     *
     * @return A set of string as filename.
     */
    public Set<String> listFilesIncluded() {
        return listOfFiles.entrySet().stream()
            .filter((k) -> !k.getValue().fileDiff.equals("-deleted-"))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
}
