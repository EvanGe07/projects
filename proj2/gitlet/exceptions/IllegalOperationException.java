package gitlet.exceptions;

/**
 * Notifies an illegal operation.
 */
public class IllegalOperationException extends Exception {
    public IllegalOperationException(String msg) {
        super(msg);
    }
}
