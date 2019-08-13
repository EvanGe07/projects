package bearmaps.utils;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * A range of integer, both bounds inclusive.
 * This is a helper class for rastering. Make that code look cleaner.
 */
public class IntRange implements Iterable<Integer> {
    private int start;
    private int end;

    public IntRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new IntRangeIterator(start, end);
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
        for (int i = start; i <= end; i++) {
            action.accept(i);
        }
    }

    /* Size from start to end, inclusive*/
    public int size() {
        return end - start + 1;
    }

    public int trim(int i) {
        return i - start;
    }

    class IntRangeIterator implements Iterator<Integer> {

        int curr;
        int start;
        int end;

        IntRangeIterator(int start, int end) {
            this.start = start;
            this.end = end;
            this.curr = start;
        }

        @Override
        public boolean hasNext() {
            return curr <= end;
        }

        @Override
        public Integer next() {
            curr += 1;
            return curr - 1;
        }
    }
}
