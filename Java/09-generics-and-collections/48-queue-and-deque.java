/* ============================================================================
 * 48 - Queue, Deque and PriorityQueue
 * ----------------------------------------------------------------------------
 * Companion lesson: 48-queue-and-deque.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/48-queue-and-deque.java
 *
 * Lesson 45 said "use ArrayDeque instead of LinkedList for a queue or stack"
 * and asked you to take it on faith. Section 3 measures it. Section 4 catches
 * the single most common PriorityQueue mistake: assuming it prints sorted.
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

class QueueAndDeque {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE TWO FAMILIES: THROWING VS SAFE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - Queue HAS TWO METHODS FOR EVERYTHING, ON PURPOSE");
        System.out.println("=".repeat(74));

        System.out.println("    add(e) / remove()  / element()  -> THROW on failure");
        System.out.println("    offer(e)/ poll()    / peek()      -> return a SPECIAL VALUE");
        System.out.println();

        Queue<String> queue = new ArrayDeque<>();
        System.out.println("    offer(\"a\") on an empty queue -> " + queue.offer("a"));
        System.out.println("    poll()  (removes)             -> " + queue.poll());
        System.out.println("    poll() again, now EMPTY       -> " + queue.poll() + "   (null, no exception)");
        System.out.println("    peek() on an empty queue      -> " + queue.peek() + "   (null, no exception)");

        try {
            queue.remove();
        } catch (NoSuchElementException e) {
            System.out.println("    remove() on an empty queue    -> NoSuchElementException");
        }
        try {
            queue.element();
        } catch (NoSuchElementException e) {
            System.out.println("    element() on an empty queue   -> NoSuchElementException");
        }
        System.out.println();
        System.out.println("    USE offer/poll/peek UNLESS an empty queue is genuinely a bug in");
        System.out.println("    your program - then let add/remove/element throw and say so.");


        /* ====================================================================
         * SECTION 2 - Deque: BOTH ENDS, SO IT IS A QUEUE AND A STACK
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - Deque: ONE INTERFACE, TWO SHAPES");
        System.out.println("=".repeat(74));

        Deque<Integer> deque = new ArrayDeque<>();
        deque.addFirst(2);
        deque.addFirst(1);
        deque.addLast(3);
        System.out.println("    addFirst(2), addFirst(1), addLast(3) -> " + deque);
        System.out.println("    peekFirst() / peekLast()             -> "
                + deque.peekFirst() + " / " + deque.peekLast());

        System.out.println();
        System.out.println("    AS A STACK (push/pop = addFirst/removeFirst):");
        Deque<String> stack = new ArrayDeque<>();
        stack.push("first");
        stack.push("second");
        stack.push("third");
        System.out.println("      push first, second, third -> " + stack);
        System.out.println("      pop()                      -> " + stack.pop()
                + "   (LIFO - last pushed, first out)");
        System.out.println("      stack is now                -> " + stack);

        System.out.println();
        System.out.println("    AS A QUEUE (offer/poll = offerLast/pollFirst, FIFO):");
        Deque<String> asQueue = new ArrayDeque<>();
        asQueue.offer("first");
        asQueue.offer("second");
        asQueue.offer("third");
        System.out.println("      offer first, second, third -> " + asQueue);
        System.out.println("      poll()                      -> " + asQueue.poll()
                + "   (FIFO - first offered, first out)");
        System.out.println("      queue is now                 -> " + asQueue);

        System.out.println();
        System.out.println("    java.util.Stack (legacy, extends Vector, lesson 26/45) does the");
        System.out.println("    same job as an ArrayDeque used as a stack. The JDK's own");
        System.out.println("    Javadoc for Deque recommends ArrayDeque over Stack anyway -");
        System.out.println("    see Section 3 for the real, measured reasons why.");


        /* ====================================================================
         * SECTION 3 - MEASURING LESSON 45's CLAIM: ArrayDeque VS LinkedList
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - ArrayDeque VS LinkedList VS Stack, FOR REAL");
        System.out.println("=".repeat(74));

        int ops = 2_000_000;

        long arrayDequeStackMillis = timeStack(new ArrayDeque<>(), ops);
        long linkedListStackMillis = timeStack(new LinkedList<>(), ops);
        long legacyStackMillis = timeLegacyStack(new Stack<>(), ops);

        System.out.printf("    %,d push()+pop() pairs, used as a STACK:%n", ops);
        System.out.println("      ArrayDeque   -> " + arrayDequeStackMillis + " ms");
        System.out.println("      LinkedList   -> " + linkedListStackMillis + " ms");
        System.out.println("      Stack        -> " + legacyStackMillis + " ms");

        long arrayDequeQueueMillis = timeQueue(new ArrayDeque<>(), ops);
        long linkedListQueueMillis = timeQueue(new LinkedList<>(), ops);

        System.out.println();
        System.out.printf("    %,d offer()+poll() pairs, used as a QUEUE:%n", ops);
        System.out.println("      ArrayDeque   -> " + arrayDequeQueueMillis + " ms");
        System.out.println("      LinkedList   -> " + linkedListQueueMillis + " ms");
        System.out.println();
        System.out.println("    ArrayDeque clearly beats LinkedList in BOTH roles, for the same");
        System.out.println("    reason lesson 45 measured for ArrayList: a resizable circular");
        System.out.println("    ARRAY has no per-element Node allocation and no pointer-chasing.");
        System.out.println();
        System.out.println("    AN HONEST SURPRISE: Stack is NOT reliably slower than ArrayDeque");
        System.out.println("    here - run this file a few times and watch the two trade places.");
        System.out.println("    Stack's synchronized methods use UNCONTENDED locks in this");
        System.out.println("    single-threaded test, and modern JVMs make those nearly free");
        System.out.println("    (biased/thin locking). The real argument against Stack is not");
        System.out.println("    'measurably slower here' - it is (1) that same synchronization");
        System.out.println("    becomes real, measurable contention overhead the moment MULTIPLE");
        System.out.println("    threads actually share one Stack, and (2) Stack extends Vector,");
        System.out.println("    so it also exposes get(i), insertElementAt(i) and every other");
        System.out.println("    List method - nothing stops code from breaking LIFO discipline");
        System.out.println("    by indexing into the middle of what is supposed to be a stack.");
        System.out.println("    ArrayDeque's API has no such escape hatch.");


        /* ====================================================================
         * SECTION 4 - PriorityQueue: A HEAP, NOT A SORTED LIST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE #1 PriorityQueue MISTAKE: EXPECTING SORTED toString()");
        System.out.println("=".repeat(74));

        PriorityQueue<Integer> heap = new PriorityQueue<>();
        int[] insertOrder = {50, 20, 80, 10, 30, 90, 5};
        for (int value : insertOrder) {
            heap.add(value);
        }
        System.out.println("    inserted -> " + java.util.Arrays.toString(insertOrder));
        System.out.println("    heap.toString() / iteration -> " + heap);
        System.out.println();
        System.out.println("    That is NOT sorted order. PriorityQueue only GUARANTEES that");
        System.out.println("    peek()/poll() return the SMALLEST element - internally it is a");
        System.out.println("    BINARY HEAP stored in a flat array, where the only structural");
        System.out.println("    rule is 'every parent <= both its children'. Iterating the");
        System.out.println("    array walks the HEAP's internal layout, not sorted order.");

        System.out.println();
        System.out.println("    poll() REPEATEDLY, however, comes out perfectly sorted:");
        PriorityQueue<Integer> forPolling = new PriorityQueue<>(heap);
        StringBuilder polledOrder = new StringBuilder();
        while (!forPolling.isEmpty()) {
            polledOrder.append(forPolling.poll()).append(" ");
        }
        System.out.println("      " + polledOrder.toString().strip()
                + "   <- THIS is how you get sorted output from a heap");

        System.out.println();
        System.out.println("    MIN-HEAP is the default (natural ordering, smallest first).");
        System.out.println("    A MAX-HEAP is one line with a reversed Comparator:");
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        for (int value : insertOrder) {
            maxHeap.add(value);
        }
        System.out.println("      new PriorityQueue<>(Comparator.reverseOrder())");
        System.out.println("      peek() -> " + maxHeap.peek() + "   (the LARGEST, not smallest)");


        /* ====================================================================
         * SECTION 5 - A REAL USE: TOP-K WITH A BOUNDED HEAP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - A REAL USE: THE TOP-K PATTERN");
        System.out.println("=".repeat(74));

        int[] scores = {42, 17, 88, 5, 63, 91, 24, 76, 8, 55, 99, 31};
        int k = 3;

        System.out.println("    scores -> " + java.util.Arrays.toString(scores));
        System.out.println("    finding the top " + k + " WITHOUT sorting the whole array:");
        System.out.println();
        System.out.println("    keep a MIN-heap of size k - if a new value beats the heap's");
        System.out.println("    smallest, evict the smallest and insert the new one. After");
        System.out.println("    scanning everything, the heap holds exactly the top k.");

        PriorityQueue<Integer> topK = new PriorityQueue<>();
        for (int value : scores) {
            topK.add(value);
            if (topK.size() > k) {
                topK.poll();   // evict the current smallest of the k we're keeping
            }
        }
        System.out.println();
        System.out.println("      top " + k + " (any order, from the heap) -> " + topK);

        java.util.List<Integer> sortedTopK = new java.util.ArrayList<>(topK);
        sortedTopK.sort(Comparator.reverseOrder());
        System.out.println("      top " + k + " (sorted for display)        -> " + sortedTopK);
        System.out.println();
        System.out.println("    Cost: O(n log k) - scanning n elements, each heap operation");
        System.out.println("    O(log k). Sorting the whole array first would be O(n log n).");
        System.out.println("    For k much smaller than n, the difference is real.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 48.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Times {@code ops} push-then-pop pairs on a {@code Deque} used as a
     * stack.
     *
     * @param deque the deque to use
     * @param ops   how many push/pop pairs
     * @return elapsed milliseconds
     */
    static long timeStack(Deque<Integer> deque, int ops) {
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            deque.push(i);
            deque.pop();
        }
        long elapsed = System.nanoTime() - start;
        return elapsed / 1_000_000;
    }

    /**
     * Times {@code ops} push-then-pop pairs on the legacy, synchronized
     * {@code Stack}.
     *
     * @param stack the stack to use
     * @param ops   how many push/pop pairs
     * @return elapsed milliseconds
     */
    static long timeLegacyStack(Stack<Integer> stack, int ops) {
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            stack.push(i);
            stack.pop();
        }
        long elapsed = System.nanoTime() - start;
        return elapsed / 1_000_000;
    }

    /**
     * Times {@code ops} offer-then-poll pairs on a {@code Queue}.
     *
     * @param queue the queue to use
     * @param ops   how many offer/poll pairs
     * @return elapsed milliseconds
     */
    static long timeQueue(Queue<Integer> queue, int ops) {
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            queue.offer(i);
            queue.poll();
        }
        long elapsed = System.nanoTime() - start;
        return elapsed / 1_000_000;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Implement a palindrome checker using ONLY a Deque: push each character,
 *    then compare pollFirst() against pollLast() until the deque has fewer
 *    than two elements left.
 *
 * 2. Section 3 measures push+pop and offer+poll interleaved with no other
 *    elements present. Change it to first fill the structure with 500,000
 *    elements, THEN time 500,000 more operations. Does the gap change?
 *
 * 3. Write a task scheduler: a Task record with a priority int, add 10 tasks
 *    in random priority order to a PriorityQueue, then poll() them all and
 *    confirm they come out highest-priority first.
 *
 * 4. Section 5's top-K pattern evicts the SMALLEST when the heap exceeds
 *    size k. Explain in one sentence why that is correct for finding the
 *    LARGEST k values (it is not a typo).
 *
 * 5. Implement breadth-first traversal of a small graph (an
 *    int -> List<Integer> adjacency map) using an ArrayDeque as the frontier
 *    queue and a HashSet (lesson 46) to track visited nodes.
 * ============================================================================
 */
