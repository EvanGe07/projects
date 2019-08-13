package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class is responsible for capturing a file.
 * The content at the time of capturing will be stored in a blob.
 */
public class FileDiff implements Serializable {

    /**
     * Hashcode of this snapshot.
     */
    private String hash;

    /**
     * The stored file.
     */
    byte[] fileStore;

    /**
     * Type of difference.
     */
    DiffType type;

    /**
     * Default constructor. Capture the content of file and store it. Calculates hash code.
     *
     * @param type      Type of diff. Should be add, del, or mod.
     *                  This is identified by comparing working directory with file map.
     * @param fileAfter The path of file after the change. Should be null if type == del.
     */
    FileDiff(DiffType type, File fileAfter) {
        this.type = type;
        if (fileAfter != null) {
            fileStore = Utils.readContents(fileAfter);
            recalcHash();
        } else {
            hash = "-deleted-";
        }
        save();
    }

    /**
     * Write the content stored in this filediff to filePath.
     * Destination will be created if it doesn't exist.
     *
     * @param filePath The file to write contents to.
     * @return True if operation is successful.
     */
    public boolean checkout(File filePath) {
        if (type == DiffType.del || hashCodeString().equals("-deleted-")) {
            return Utils.restrictedDelete(filePath);
        } else {
            try {
                Utils.writeContents(filePath, fileStore);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    /**
     * Recaulculate the hash of this filediff.
     * Internal helper function,
     */
    private void recalcHash() {
        hash = Utils.sha1(fileStore);
    }

    public String hashCodeString() {
        return hash;
    }

    /**
     * Simply returns the pre-calculated hashcode
     *
     * @return The hashcode of this FileDiff.
     */
    @Override
    @Deprecated
    public int hashCode() {
        return Integer.parseInt(hash, 16);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FileDiff) {
            return this.hashCodeString().equals(((FileDiff) other).hashCodeString());
        } else {
            return false;
        }
    }

    /**
     * Save the instance to {@code .gitlet/filediff/<hash>.obj}
     */
    public void save() {
        String fdDir = GitletWorkspace.getGitFileDiffDir();
        File thisFD = new File(fdDir + File.separator + hash + ".obj");
        if (!thisFD.exists()) {
            try {
                thisFD.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            FileOutputStream fout = new FileOutputStream(thisFD);
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
     * Given the hashcode, the FileDiff object will be loaded
     * from file {@code .gitlet/filediff/<hash>.obj}
     *
     * @param hash hashcode of the FileDiff object to load.
     * @return A FileDiff object loaded from disk.
     */
    public static FileDiff load(String hash) {
        String fdDir = GitletWorkspace.getGitFileDiffDir();
        File thisFD = new File(fdDir + File.separator + hash + ".obj");
        if (!thisFD.exists()) {
            try {
                thisFD.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            FileInputStream fout = new FileInputStream(thisFD);
            ObjectInputStream oout = new ObjectInputStream(fout);
            FileDiff toReturn = (FileDiff) oout.readObject();
            oout.close();
            return toReturn;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
