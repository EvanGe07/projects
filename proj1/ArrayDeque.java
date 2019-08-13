public class ArrayDeque<T> implements Deque<T> {

    /**
     * The index of the first element in the deque.
     * Elements in the array before firstP are meaningless.
     */
    private int firstP;

    /**
     * The index of the last element in the deque.
     * Elements in the array after lastP are meaningless.
     */
    private int lastP;

    /**
     * Core data structure for the deque.
     * The zeroeth element is at queue[firstP]
     * The last element is at queue[lastP]
     */
    private T[] queue;

    private final int BEGIN_SIZE = 8;
    private final int SIZE_INCREMENT = 24;

    /**
     * Default constructor. Constructs an empty ArrayDeque.
     */
    public ArrayDeque() {
        firstP = 5;
        lastP = 4;
        queue = (T[]) new Object[BEGIN_SIZE];
    }

//    public ArrayDeque(T... data) {
//        queue = Arrays.copyOf(data, data.length);
//        firstP = 0;
//        lastP = queue.length - 1;
//    }

    public static <T> ArrayDeque<T> of(T... data) {
        ArrayDeque<T> newDeque = new ArrayDeque<T>();
        newDeque.queue = (T[]) new Object[data.length];
        System.arraycopy(data, 0, newDeque.queue, 0, data.length);
        newDeque.firstP = 0;
        newDeque.lastP = newDeque.queue.length - 1;
        return newDeque;
    }

    /**
     * Get the size of the current deque.
     *
     * @return int Size of the deque.
     */
    @Override
    public int size() {
        return lastP - firstP + 1;
    }

    /**
     * Return the object at given *index*.
     *
     * @param index Index to get. Null if index is invalid.
     */
    @Override
    public T get(int index) {
        if (index + firstP > lastP) {
            return null;
        } else {
            return queue[index + firstP];
        }
    }

    /**
     * Add an element at the beginning.
     *
     * @param data Element to add.
     */
    @Override
    public void addFirst(T data) {
        if (firstP == 0) {
            //extendFirst();
            //geometricExtendFirst();
            geometricExtendBoth();
        }
        firstP--;
        queue[firstP] = data;
    }

    /**
     * Add an element at the end.
     *
     * @param data Element to add.
     */
    @Override
    public void addLast(T data) {
        if (lastP == queue.length - 1) {
            //extendLast();
            //geometricExtendLast();
            geometricExtendBoth();
        }
        lastP++;
        queue[lastP] = data;
    }

    /**
     * If the list is empty.
     *
     * @return True if empty, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return lastP < firstP;
    }

    /**
     * Prints the whole deque to System.out.
     */
    @Override
    public void printDeque() {
        System.out.println(toString());
    }

    /**
     * Remove an element at the beginning.
     *
     * @return Element removed from the beginning.
     */
    @Override
    public T removeFirst() {
        if (lastP < firstP) {
            //throw new IllegalStateException("Cannot remove from an empty list.");
            return null;
        } else {
            firstP++;
            T removed = queue[firstP - 1];
            queue[firstP - 1] = null;
            lighterShrink();
            return removed;
        }
    }

    /**
     * Add an element at the end.
     *
     * @return Element removed from the end.
     */
    @Override
    public T removeLast() {
        if (lastP < firstP) {
            //throw new IllegalStateException("Cannot remove from an empty list.");
            return null;
        } else {
            lastP--;
            T removed = queue[lastP + 1];
            queue[lastP + 1] = null;
            lighterShrink();
            return removed;
        }
    }

    private void extendFirst() {
        try {
            T[] newQueue2 = (T[]) new Object[queue.length + SIZE_INCREMENT];
            System.arraycopy(queue, 0, newQueue2, SIZE_INCREMENT, queue.length);
            queue = newQueue2;
            firstP += SIZE_INCREMENT;
            lastP += SIZE_INCREMENT;
        } catch (ClassCastException e) {
            System.out.println("Error happened during casting Object[] to T[] during extension");
            e.printStackTrace();
        }
    }

    private void geometricExtendFirst() {
        try {
            int sizeIncrement = queue.length;
            int newSize = sizeIncrement + queue.length;
            int datLen = size();
            T[] newQueue2 = (T[]) new Object[queue.length + sizeIncrement];
            System.arraycopy(queue, 0, newQueue2, sizeIncrement, queue.length);
            queue = newQueue2;
            firstP += sizeIncrement;
            lastP += sizeIncrement;

        } catch (ClassCastException e) {
            System.out.println("Error happened during casting Object[] to T[] during extension");
            e.printStackTrace();
        }
    }

    private void geometricExtendBoth() {
        if (size() == 0) {
            queue = (T[]) new Object[BEGIN_SIZE];
            firstP = 5;
            lastP = 4;
        } else {
            // Copy firstP - lastP to the center of new queue.

            // If the size is too small, there will be some rounding errors.
            int newSize = size() * 2;
            if (newSize < BEGIN_SIZE) {
                newSize = BEGIN_SIZE;
            }

            T[] newQueue = (T[]) new Object[newSize];
            /**
             * The current array:
             * First Data: firstP; Last Data; lastP
             * Length of data: firstP - lastP +1
             */
            int dataCount = size();

            /**
             * The new array:
             * First data: (total size)/2 - dataCount/2
             * Last data: first + len.
             */

            int newFirstP = Math.floorDiv(newSize, 2) - Math.floorDiv(dataCount, 2);
            System.arraycopy(queue, firstP, newQueue, newFirstP, dataCount);
            queue = newQueue;
            firstP = newFirstP;
            lastP = firstP + dataCount - 1;
        }
    }

    /**
     * Debug with this
     */
    @Deprecated
    public void testExtendFirst() {
        geometricExtendFirst();
    }

    @Deprecated
    public void testExtendBoth() {
        geometricExtendBoth();
    }

    private void extendLast() {
        try {
            T[] newQueue2 = (T[]) new Object[queue.length + SIZE_INCREMENT];
            System.arraycopy(queue, 0, newQueue2, 0, queue.length);
            queue = newQueue2;
        } catch (ClassCastException e) {
            System.out.println("Error happened during casting Object[] to T[] during extension");
            e.printStackTrace();
        }
    }

    private void geometricExtendLast() {
        try {
            int sizeIncrement = queue.length;
            T[] newQueue2 = (T[]) new Object[queue.length + sizeIncrement];
            System.arraycopy(queue, 0, newQueue2, 0, queue.length);
            queue = newQueue2;
        } catch (ClassCastException e) {
            System.out.println("Error happened during casting Object[] to T[] during extension");
            e.printStackTrace();
        }
    }

    /**
     * Debug with this
     */
    @Deprecated
    public void testExtendLast() {
        geometricExtendLast();
    }

    /**
     * Shrink both ends sligtly. T[] newQueue2 = (T[])new Object[queue.length+SIZE_INCREMENT];
     * System.arraycopy(queue,0,newQueue2,0,queue.length);
     * If there're more than SIZE_INCREMENT blank spaces on an end,
     * Shirnk that end by SIZE_INCREMENT.
     */
    private void lightShrink() {
        if (firstP > SIZE_INCREMENT) {
            int beta = Math.floorDiv(firstP, SIZE_INCREMENT);
            int decValue = SIZE_INCREMENT * beta;
            T[] newQueue = (T[]) new Object[queue.length - decValue];
            System.arraycopy(queue, decValue, newQueue, 0, queue.length - decValue);
            queue = newQueue;
            firstP -= decValue;
            lastP -= decValue;
        }

        if (queue.length - lastP - 1 > SIZE_INCREMENT) {
            int beta = Math.floorDiv(queue.length - lastP - 1, SIZE_INCREMENT);
            int decValue = SIZE_INCREMENT * beta;
            T[] newQueue = (T[]) new Object[queue.length - decValue];
            System.arraycopy(queue, 0, newQueue, 0, queue.length - decValue);
            queue = newQueue;
        }
    }

    private void lighterShrink() {
        if (queue.length > BEGIN_SIZE && size() < queue.length / 2) {
            // Copy firstP - lastP to the center of new queue.

            // If the size is too small, there will be some rounding errors.
            int newSize = Math.floorDiv(queue.length, 2);
            if (newSize < BEGIN_SIZE) {
                newSize = BEGIN_SIZE;
            }

            T[] newQueue = (T[]) new Object[newSize];
            /**
             * The current array:
             * First Data: firstP; Last Data; lastP
             * Length of data: firstP - lastP +1
             */
            int dataCount = size();

            /**
             * The new array:
             * First data: (total size)/2 - dataCount/2
             * Last data: first + len.
             */

            int newFirstP = Math.floorDiv(newSize, 2) - Math.floorDiv(dataCount, 2);
            System.arraycopy(queue, firstP, newQueue, newFirstP, dataCount);
            queue = newQueue;
            firstP = newFirstP;
            lastP = firstP + dataCount - 1;
        }
    }

    @Deprecated
    public void testShrink() {
        lighterShrink();
    }

    @Override
    public boolean equals(Object anything) {
        if (anything instanceof Deque) {
            return equals((Deque<T>) anything);
        } else {
            return false;
        }
    }

    public boolean equals(Deque<T> other) {
        if (this.size() != other.size()) {
            return false;
        } else {
            for (int i = 0; i < size(); i++) {
                if (get(i) != other.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sbu = new StringBuilder();
        for (int i = firstP; i <= lastP; i++) {
            if (queue[i] != null) {
                sbu.append(queue[i].toString() + " ");
            }
        }
        return sbu.toString().strip();
    }

    @Deprecated
    public int testGetMemorySize() {
        return queue.length;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
