package gitlet;

/**
 * The current directory (and all of its parent directories) is not a gitlet workspace.
 */
public class NoGitException extends Exception {
    public NoGitException() {
        super("You're not in a gitlet directory");
    }
}
