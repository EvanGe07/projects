/**
 * Test for ArrayDeque.
 * <p>
 * If you think this file looks weird, yes, yes it is.
 * The autograder REQUIRE all lines to be <100 characters.
 * Fuck you gradescope
 *
 * @author Zixi Li
 */

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ArrayDequeTest {

    @Test
    public void testExtendLast() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                        13, 14, 15, 16, 17, 18, 19, 20);
        ArrayDeque<Integer> test2 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                        13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23);
        test1.addLast(21);
        test1.addLast(22);
        test1.addLast(23);
        assertTrue(test1.equals(test2));
    }

    @Test
    public void testExtendFirst() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        ArrayDeque<Integer> test2 =
                ArrayDeque.of(-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                        10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        test1.addFirst(0);
        test1.addFirst(-1);
        test1.addFirst(-2);
        assertTrue(test1.equals(test2));
        assertEquals(test1, test2);
    }

    @Test
    public void testShrinkNothing() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        ArrayDeque<Integer> test2 =
                ArrayDeque.of(2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        ArrayDeque<Integer> test3 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        test1.testShrink();
        assertEquals(test1, test3);
        test1.removeFirst();
        assertTrue(test1.equals(test2));
        test1.testShrink();
        assertTrue(test1.equals(test2));
    }

    @Test
    public void testShrinkOnce() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        ArrayDeque<Integer> test2 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        ArrayDeque<Integer> test3 =
                ArrayDeque.of(9, 10, 11, 12, 13, 14,
                        15, 16, 17, 18, 19, 20);
        assertTrue(test1.equals(test2));
        for (int i = 1; i <= 8; i++) {
            test1.removeFirst();
        }
        assertFalse(test1.equals(test2));
        assertTrue(test1.equals(test3));
        test1.testShrink();
        assertTrue(test1.equals(test3));
    }

    @Test
    public void testRemoveFirst() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        assertEquals(1, (long) test1.removeFirst());
        assertEquals(test1,
                ArrayDeque.of(2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                        12, 13, 14, 15, 16, 17, 18, 19, 20));
        assertEquals(2, (long) test1.removeFirst());
        assertEquals(test1,
                ArrayDeque.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                        13, 14, 15, 16, 17, 18, 19, 20));
        assertEquals(3, (long) test1.removeFirst());
        assertEquals(test1,
                ArrayDeque.of(4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                        14, 15, 16, 17, 18, 19, 20));
        assertEquals(4, (long) test1.removeFirst());
        assertEquals(test1,
                ArrayDeque.of(5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                        15, 16, 17, 18, 19, 20));
    }

    @Test
    public void testRemovelast() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        assertEquals(20, (long) test1.removeLast());
        assertEquals(test1,
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19));
        assertEquals(19, (long) test1.removeLast());
        assertEquals(test1,
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18));
        assertEquals(18, (long) test1.removeLast());
        assertEquals(test1,
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17));
        assertEquals(17, (long) test1.removeLast());
        assertEquals(test1,
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16));
    }

    @Test
    public void testIsEmpty() {
        ArrayDeque<Integer> test1 = new ArrayDeque<>();
        assertTrue(test1.isEmpty());

        ArrayDeque<Integer> test2 = ArrayDeque.of(1);
        assertFalse(test2.isEmpty());
        test2.removeLast();
        assertTrue(test2.isEmpty());
    }

    @Test
    public void testBuildLast() {
        ArrayDeque<Integer> test1 = new ArrayDeque<>();
        test1.addLast(10);
        assertEquals(test1, ArrayDeque.of(10));
        assertEquals(10, (long) test1.get(0));
        test1.addLast(20);
        assertEquals(test1, ArrayDeque.of(10, 20));
        test1.addLast(30);
        assertEquals(test1, ArrayDeque.of(10, 20, 30));
        test1.addLast(40);
        assertEquals(test1, ArrayDeque.of(10, 20, 30, 40));
    }

    @Test
    public void testBuildFirst() {
        ArrayDeque<Integer> test1 = new ArrayDeque<>();
        test1.addFirst(10);
        assertEquals(test1, ArrayDeque.of(10));
        test1.addFirst(20);
        assertEquals(test1, ArrayDeque.of(20, 10));
        test1.addFirst(30);
        assertEquals(test1, ArrayDeque.of(30, 20, 10));
        test1.addFirst(40);
        assertEquals(test1, ArrayDeque.of(40, 30, 20, 10));
    }

    @Test
    public void testRemoveToEmpty() {
        ArrayDeque<Integer> test1 = ArrayDeque.of(1, 2);
        assertEquals(1, (long) test1.removeFirst());
        assertEquals(2, (long) test1.removeLast());
        assertNull(test1.removeFirst());
        assertNull(test1.removeLast());
        test1.addFirst(1);
        test1.addLast(2);
        assertEquals(test1, ArrayDeque.of(1, 2));
    }

    @Test
    public void testAbuse() {
        ArrayDeque<Integer> newDeque = new ArrayDeque<>();
        Random newRandom = new Random();
        for (int i = 0; i < 1000; i++) {
            newDeque.addFirst(newRandom.nextInt(100));
        }
        for (int i = 0; i < 250; i++) {
            newDeque.removeFirst();
        }
        for (int i = 1; i < 250; i++) {
            newDeque.removeLast();
        }
        assertTrue(newDeque.testGetMemorySize() < 750);
        System.out.println(
                String.format("ArrayTest.testAbuse()\n\tThis thing is using %d units",
                        newDeque.testGetMemorySize()
                ));
    }

    @Test
    public void testGet() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                        12, 13, 14, 15, 16, 17, 18, 19, 20);
        assertEquals(1, (long) test1.get(0));
        assertEquals(10, (long) test1.get(9));
        assertNull(test1.get(20));
        assertEquals(1, (long) test1.removeFirst());
        assertEquals(2, (long) test1.get(0));
        assertEquals(20, (long) test1.get(18));
    }

    @Test
    public void testGetFew() {
        ArrayDeque<Integer> test1 = ArrayDeque.of(1);
        assertEquals(1, test1.size());
        assertEquals(1, (long) test1.get(0));
        assertNull(test1.get(1));
        assertEquals(1, (long) test1.removeFirst());
        assertNull(test1.get(0));
    }

    @Test
    public void testEqualWeirds() {
        ArrayDeque<Integer> test1 = ArrayDeque.of(1);
        assertFalse(test1.equals("Yahooo"));
    }

    @Test
    public void testExtendUnchanged() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        ArrayDeque<Integer> test2 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        test1.testExtendFirst();
        assertEquals(test1, test2);
        test1.testExtendLast();
        assertEquals(test1, test2);
    }

    @Test
    public void testToString() {
        ArrayDeque<Integer> test1 =
                ArrayDeque.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        assertEquals(test1.toString(),
                "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20");

        ArrayDeque<Integer> test2 = new ArrayDeque<>();
        assertEquals(test2.toString(), "");
    }

    @Test
    public void testSeqOperation() {
        ArrayDeque<Integer> test = new ArrayDeque<>();
        test.addFirst(0);
        assertEquals(0, (long) test.removeLast());
        test.addFirst(2);
        test.addFirst(3);
        assertEquals(2, (long) test.get(1));
        assertEquals(3, (long) test.get(0));
        test.addLast(5);
        assertEquals(5, (long) test.get(2));
        assertEquals(5, (long) test.removeLast());
        assertEquals(3, (long) test.removeFirst());
        assertEquals(2, (long) test.get(0));
        test.addLast(10);
        assertEquals(2, (long) test.removeFirst());
        test.addFirst(12);
        assertEquals(12, (long) test.get(0));
        test.addLast(14);
        assertEquals(12, (long) test.removeFirst());
        test.addFirst(16);
        assertEquals(16, (long) test.removeFirst());
        assertEquals(10, (long) test.removeFirst());
        assertEquals(14, (long) test.removeLast());
        test.addLast(20);
        test.addLast(21);
        assertEquals(20, (long) test.removeFirst());
        assertEquals(21, (long) test.get(0));
    }

    @Test
    public void constTest() {
        System.out.println("\nConstancy Test");
        System.out.println("========================");
        Random rnd = new Random();
        for (long total = 16; total <= 128000; total *= 2) {
            ArrayDeque<Integer> tester = new ArrayDeque<>();

            //Do 10 times and take average
            int sumTime = 0;
            for (int k = 1; k <= 10; k++) {
                long start = System.nanoTime();
                for (int i = 1; i <= total; i++) {
                    int trnd = rnd.nextInt(100);
                    if (trnd < 30) {
                        tester.addFirst(rnd.nextInt(100));
                    } else if (trnd < 60) {
                        tester.addLast(rnd.nextInt(100));
                    } else if (trnd < 80) {
                        tester.removeFirst();
                    } else {
                        tester.removeLast();
                    }
                }
                long end = System.nanoTime();
                sumTime += end - start;
            }
            System.out.println(String.format(
                    "%10d   operations took %10s  nanoseconds.",
                    total, sumTime / 10
            ));
        }
    }

    @Test
    public void testNewExtend() {
        ArrayDeque<Integer> test = ArrayDeque.of(1);
        test.addLast(2);
        test.testExtendBoth();
        assertEquals(ArrayDeque.of(1, 2), test);
        test.removeLast();
    }
}
