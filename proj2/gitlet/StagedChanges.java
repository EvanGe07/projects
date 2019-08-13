package gitlet;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.stream.Collectors;

/**
 * This class represents the staging area.
 * This class will handle the job of maintaining the staging area.
 * Including saving and loading to/from a file.
 * <p>
 * This class DOES NOT implement Serializable. It will handle saving and loading changeList.
 */
public class StagedChanges {

    /* A map storing <filename, filediff>.
     * String is the relative path to the root of gitlet repo.
     * FileDiff is a FileDiff object, storing the content, hash, and type of change.
     */
    Map<String, FileDiff> changeList;

    /**
     * Default constructor. Creates an empty changeList.
     * Should do nothing.
     */
    public StagedChanges() {
        changeList = new HashMap<>();
    }

    /**
     * Add a file to the staging area.
     * Example: When executing {@code gitlet add}
     *
     * @param filename Relative path of the file.
     * @param change   A FileDiff Object.
     */
    public void add(String filename, FileDiff change) {
        if (changeList.containsKey(filename)) {
            changeList.replace(filename, change);
        } else {
            changeList.put(filename, change);
        }
    }

    /**
     * Remove a file from the staging area.
     * Example: When executing {@code gitlet checkout --}
     *
     * @param filename The relative path of the file to remove from this commit.
     */
    public void remove(String filename) {
        if (changeList.containsKey(filename)) {
            changeList.remove(filename);
        }
    }

    /**
     * Save the staging area to {@code .gitlet/staged.obj}
     */
    public void save() {
        Map obj = changeList;
        File outFile = new File(GitletWorkspace.getGitStagingPath());
        try {
            outFile.createNewFile();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(obj);
            out.close();
        } catch (IOException excp) {
            excp.printStackTrace();
        }
    }

    /**
     * Load the staging area from {@code .gitlet/staged.obj}
     */
    public static StagedChanges load() {
        File inFile = new File(GitletWorkspace.getGitStagingPath());
        StagedChanges toReturn = new StagedChanges();
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            toReturn.changeList = (Map) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            toReturn.changeList = new HashMap<String, FileDiff>();
        }
        return toReturn;
    }

    /**
     * Get the staging area ready for a commit.
     *
     * @return A map of relative filepaths and their snapshotted contents.
     */
    public Map<String, FileDiff> wrap() {
        return changeList;
    }

    public Map<String, String> getStagedList() {
        return changeList.entrySet().stream()
            .collect(Collectors.toMap(
                (e) -> e.getKey(),
                (e) -> e.getValue().hashCodeString()
            ));
    }

    /**
     * Return if the change list is empty.
     * The change list will be empty when no file has been added.
     *
     * @return True if no files have been staged.
     */
    public boolean isEmpty() {
        return changeList.isEmpty();
    }
}
