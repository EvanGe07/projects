package gitlet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GitletWorkspace {
    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        GitletWorkspace.name = name;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        GitletWorkspace.email = email;
    }

    public static String getBranchName() {
        return branchName;
    }

    public static void setBranchName(String branchName) {
        GitletWorkspace.branchName = branchName;
    }

    public static String getGitPath() {
        return gitPath;
    }

    public static void setGitPath(String gitPath) {
        GitletWorkspace.gitPath = gitPath;
    }

    /**
     * Name of the owner
     */
    private static String name;

    /**
     * Email of the owner
     */
    private static String email;

    /**
     * Name of current branch
     */
    static String branchName;

    /**
     * The path where {@code .gitlet/} folder is placed at.
     * <p>
     * This is the root directory.
     * Hence,{@code .gitlet} folder is at {@code gitpath/.gitlet}
     * </p>
     */
    private static String gitPath;

    /**
     * The File separator for current system.
     * "/" for Linux/Mac, "\" for Windows.
     */
    private static String PATH_SEPARATOR = File.separator;

    /**
     * A tracker for the files in the working directory.
     * Will remember the hashcodes for all files since gitlet last see them.
     * i.e. Keep as-is of the latest commit.
     */
    static Map<String, String> fileTracker = new HashMap<>();

    /**
     * Initializs the workspace
     * <p>
     * If the current directory is a git directory(already something in it),
     * it will read attributes from configuration files. Otherwise, it will initialize
     * the current directory to be a git directory, and create the respecting files.
     * </p>
     */
    public static void init() {
        try {
            String currentPath = new File(".").getCanonicalPath();

            /* This is the current path. Guaranteed to exist unless sky falls */
            File gitFPath = new File(currentPath);
            File gitFPathGitletDir =
                new File(currentPath + PATH_SEPARATOR + ".gitlet" + PATH_SEPARATOR);

            if (gitFPathGitletDir.exists() && gitFPathGitletDir.isDirectory()) {
                GitletWorkspace.gitPath = currentPath;
                if (!loadConfig()) {
                    createDefaultFile();
                    saveConfig();
                }
                if (!loadTracker()) {
                    fileTracker = new HashMap<>(); // Everything is new to tracker.
                    saveTracker();
                }
            } else {
                if (gitFPathGitletDir.isFile()) {
                    /* Some asshole created a .gitlet file here. Screw him */
                    gitFPathGitletDir.delete();
                }
                gitFPathGitletDir.mkdirs();
                gitPath = gitFPath.getPath();

                createDefaultFile();
                saveConfig();

                fileTracker = new HashMap<>(); // Everything is new to tracker.
                saveTracker();

                File commitDir = new File(getGitCommitsDir());
                commitDir.mkdirs();

                File branchesDir = new File(getGitBranchesDir());
                branchesDir.mkdirs();
                Branch newMaster = new Branch("master");
                newMaster.save();
                branchName = "master";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() throws NoGitException {
        try {
            String currentPath = new File(".").getCanonicalPath();

            /* This is the current path. Guaranteed to exist unless sky falls */
            File gitFPath = new File(currentPath);
            File gitFPathGitletDir =
                new File(currentPath + PATH_SEPARATOR + ".gitlet" + PATH_SEPARATOR);

            if (gitFPathGitletDir.exists() && gitFPathGitletDir.isDirectory()) {
                GitletWorkspace.gitPath = currentPath;
                if (!loadConfig()) {
                    createDefaultFile();
                    saveConfig();
                }
                if (!loadTracker()) {
                    fileTracker = new HashMap<>(); // Everything is new to tracker.
                    saveTracker();
                }
            } else {
                throw new NoGitException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the tracker to local storage
     *
     * @return True if success.
     */
    public static boolean saveTracker() {
        File gitTracker = new File(getGitTrackerPath("obj"));
        try {
            gitTracker.createNewFile();
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(gitTracker));
            oout.writeObject(fileTracker);
            oout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Load the file tracker from local storage.
     *
     * @return True if success.
     */
    public static boolean loadTracker() {
        File gitTracker = new File(getGitTrackerPath("obj"));
        if (gitTracker.exists()) {
            try {
                ObjectInputStream trkIn = new ObjectInputStream(new FileInputStream(gitTracker));
                fileTracker = (HashMap<String, String>) trkIn.readObject();
                trkIn.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (ClassCastException e) {
                /* Corrupted File */
                e.printStackTrace();
                return false;
            }
        } else {
            /* Tracker not found. Assumed first run. */
            return false;
        }
        return true;
    }

    /**
     * Save the configuration.
     * <p>This method will not save the file tracker.</p>
     *
     * @return True if success.
     */
    public static boolean saveConfig() {
        return saveConfigXML();
    }

    /**
     * Load the configuration.
     * <p>This method will not save the file tracker.</p>
     *
     * @return True if success.
     */
    public static boolean loadConfig() {
        return loadConfigXML();
    }

    /**
     * Save the configuration to an XML File.
     * <p>
     * Default path: {@code .gitlet/workspace.xml}.
     * </p>
     */
    public static boolean saveConfigXML() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();

            Element gitletRoot = document.createElement("gitlet");

            Element user = document.createElement("user");
            Element xmlname = document.createElement("name");
            Element xmlemail = document.createElement("email");
            xmlname.appendChild(document.createTextNode(GitletWorkspace.name));
            xmlemail.appendChild(document.createTextNode(GitletWorkspace.email));
            user.appendChild(xmlname);
            user.appendChild(xmlemail);
            gitletRoot.appendChild(user);

            Element currBranch = document.createElement("current-branch");
            currBranch.appendChild(document.createTextNode(GitletWorkspace.branchName));
            gitletRoot.appendChild(currBranch);

            document.appendChild(gitletRoot);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            DOMSource newSrc = new DOMSource(document);

            File gitConfigFile = new File(getGitWkspPath("xml"));
            gitConfigFile.createNewFile();
            StreamResult outStream = new StreamResult(gitConfigFile);

            t.transform(newSrc, outStream);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Load the configuration from a XML file.
     * <p>Default path: {@code .gitlet/workspace.xml}</p>
     *
     * @return True if success
     */
    public static boolean loadConfigXML() {
        File gitConfigFile = new File(getGitWkspPath("xml"));
        if (gitConfigFile.exists()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(gitConfigFile);
                document.getDocumentElement().normalize();
                NodeList gitletRoot =
                    document.getElementsByTagName("gitlet").item(0).getChildNodes();
                for (int index = 0; index < gitletRoot.getLength(); index++) {
                    if (gitletRoot.item(index).getNodeName().equals("user")) {
                        Element user = (Element) gitletRoot.item(index);
                        name = user.getElementsByTagName("name").item(0).getTextContent();
                        email = user.getElementsByTagName("email").item(0).getTextContent();
                    } else if (gitletRoot.item(index).getNodeName().equals("current-branch")) {
                        branchName = gitletRoot.item(index).getTextContent();
                    }
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (SAXException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            createDefaultFile();
            saveConfigXML();
        }
        return true;
    }

    /**
     * Create a default configuration file based on username and computername.
     * Will be called when configuration fil is not found.
     */
    public static void createDefaultFile() {
        name = System.getProperty("user.name");
        email = name + "@" + getComputerName();
        branchName = "master";
    }

    /**
     * Get the name of the current computer.
     * From: https://stackoverflow.com/questions/7883542/getting-the-computer-name-in-java
     *
     * @return name of the computer.
     */
    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME")) {
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            return env.get("HOSTNAME");
        } else {
            return "Unknown Computer";
        }
    }

    /**
     * Get the absolute path for {@code .gitlet/workspace.xml/yml/anything-else}
     * <p>Internal helper method. Helps cleaning up the code. </p>
     *
     * @return An absolute path pointing to workspace.ext
     */
    private static String getGitWkspPath(String ext) {
        return ".gitlet" + PATH_SEPARATOR + "workspace." + ext;
    }

    /**
     * Get the absolute path for {@code .gitlet/workspace.xml/yml/anything-else}
     * <p>Internal helper method. Helps cleaning up the code.</p>
     *
     * @return An absolute path pointing to workspace.ext
     */
    private static String getGitTrackerPath(String ext) {
        return gitPath + PATH_SEPARATOR + ".gitlet" + PATH_SEPARATOR + "tracker." + ext;
    }

    /**
     * Get the absoulte path for the staging area. Does not guarantee file exists.
     *
     * @return An absoulte path to "staged.obj".
     */
    public static String getGitStagingPath() {
        return gitPath + PATH_SEPARATOR + ".gitlet" + PATH_SEPARATOR + "staged.obj";
    }

    /**
     * Get the absoulte path for the commits directory.
     * Create a new folder if it does not exist.
     *
     * @return An absoulte path to "commmits/" folder.
     */
    public static String getGitCommitsDir() {
        File commDir = new File(gitPath + File.separator + ".gitlet"
            + File.separator + "commits" + File.separator);
        if (commDir.exists() && commDir.isDirectory()) {
            return commDir.getPath();
        } else {
            commDir.delete();
            commDir.mkdirs();
            return commDir.getPath();
        }
    }

    /**
     * Get the absoulte path for the branches storage.
     * Create a new folder if branches/ does not exist.
     *
     * @return An absoulte path to "branches/" folder.
     */
    public static String getGitBranchesDir() {
        File branDir = new File(gitPath + File.separator + ".gitlet"
            + File.separator + "branches" + File.separator);
        if (branDir.exists() && branDir.isDirectory()) {
            return branDir.getPath();
        } else {
            branDir.delete();
            branDir.mkdirs();
            return branDir.getPath();
        }
    }

    /**
     * Get the absoulte path of the file in this git repo, provided a relative path.
     *
     * @param relativePath Relative path of the file
     * @return Canonical Path of file.
     */
    public static String getCanonicalPath(String relativePath) {
        //return gitPath + PATH_SEPARATOR + relativePath;
        return relativePath;
    }

    /**
     * Get the absoulte file pointer from a relative path.
     *
     * @param relativePath Relative path of the file
     * @return File pointer. Does not guarantee existence of file.
     */
    public static File getCanonicalFile(String relativePath) {
        return new File(getCanonicalPath(relativePath));
    }

    /**
     * Compare the file at relPath to file tracker, and generate a FileDiff.
     *
     * @param relPath Relative path. This file will be read and stored.
     * @return A FileDiff object representing the difference.
     */
    public static FileDiff generateFileDiff(String relPath) {
        File tmpFile = new File(getCanonicalPath(relPath));
        StagedChanges sgArea = StagedChanges.load();
        if (tmpFile.exists()) {
            String hash = Utils.sha1File(tmpFile);
            if (fileTracker.containsKey(relPath) && !fileTracker.get(relPath).equals("-deleted-")) {
                /* The file is included in the tracker */
                if (fileTracker.get(relPath).equals(hash)) {
                    /* No change is done. Do nothing. */
                    /* If it's in the staging area, remove it. */
                    sgArea.remove(relPath);
                    sgArea.save();
                    return null;
                } else {
                    /* Modified */
                    return new FileDiff(DiffType.mod, tmpFile);
                }
            } else {
                /* File is not in the tracker. Seen as add. */
                return new FileDiff(DiffType.add, tmpFile);
            }
        } else {
            return null;
        }
    }

    /**
     * Read the file at {@code relPath}. and record it in FileTracker.
     *
     * @param relPath Relative path to the file.
     */
    public static void resetTracker(String relPath) {
        File tmpFile = new File(getCanonicalPath(relPath));
        if (tmpFile.exists()) {
            fileTracker.put(
                relPath,
                Utils.sha1(Utils.readContents(tmpFile))
            );
        } else {
            fileTracker.remove(relPath);
        }
        saveTracker();
    }

    /**
     * Read the files at {@code relPath[]}. and record them in FileTracker.
     */
    public static void resetTracker(String[] relPath) {
        for (String x : relPath) {
            resetTracker(x);
        }
    }

    /**
     * Return a map of files in the working directory and their hash
     */
    public static Map<String, String> listFiles() {
        List<String> lstFiles = Utils.plainFilenamesIn(gitPath);
        return lstFiles.stream()
            .collect(Collectors.toMap(
                (e) -> e,
                (e) -> Utils.sha1File(new File(e))
            ));
        // Function::identity does not work here somehow.
    }

    /**
     * The path for storing FileDiff
     **/
    public static String getGitFileDiffDir() {
        File branDir = new File(gitPath + File.separator + ".gitlet"
            + File.separator + "filediff" + File.separator);
        if (branDir.exists() && branDir.isDirectory()) {
            return branDir.getPath();
        } else {
            branDir.delete();
            branDir.mkdirs();
            return branDir.getPath();
        }
    }

    /**
     * Return a list of untracked files.
     *
     * @return A set of filenames of untracked files.
     */
    public static Set<String> getUntrackedFiles() {
        try {
            GitletWorkspace.load();
        } catch (NoGitException e) {
            e.printStackTrace();
            return null;
        }

        Map<String, String> fileList = GitletWorkspace.listFiles();
        StagedChanges sgArea = StagedChanges.load();
        Map<String, String> sgList = sgArea.getStagedList();

        Set<String> fileAdded = new HashSet<>();

        fileList.forEach((key, value) -> {
            if (!sgList.containsKey(key)) {
                if (!GitletWorkspace.fileTracker.containsKey(key)
                    || GitletWorkspace.fileTracker.get(key).equals("-deleted-")) {
                    /* This file is not in any previous commits. It's untracked. */
                    fileAdded.add(key);
                }
            }
        });

        return fileAdded;
    }

    /**
     * Load the file tracker from a commit.
     *
     * @param c The commit to load from.
     */
    public static void loadTrackerFromCommit(Commit c) {
        fileTracker.clear();
        c.listOfFiles.forEach((filename, record) -> {
            fileTracker.put(filename, record.fileDiff);
        });
        saveTracker();
    }
}
