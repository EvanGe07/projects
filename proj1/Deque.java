/**
 * Interface for a Deque (double-sided queue) data structure.
 *
 * @author Zixi Li
 */

public interface Deque<T> {
    /**
     * Get the size of the current deque.
     * @return int Size of the deque.
     */
    default int size() {
        return 0;
    }

    /**
     * Return the object at given *index*.
     * @param index Index to get. Null if index is invalid.
     */
    T get(int index);

    /**
     * Add an element at the beginning.
     * @param data Element to add.
     */
    void addFirst(T data);

    /**
     * Add an element at the end.
     * @param data Element to add.
     */
    void addLast(T data);

    /**
     * If the list is empty.
     * @return True if empty, false otherwise.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Prints the whole deque to System.out.
     */
    void printDeque();

    /**
     * Remove an element at the beginning.
     * @return Element removed from the beginning.
     */
    T removeFirst();

    /**
     * Add an element at the end.
     * @return Element removed from the end.
     */
    T removeLast();

}
