/* ============================================================================
 * 20 - RECURSION AND THE CALL STACK
 * ----------------------------------------------------------------------------
 * Companion lesson: 20-recursion.md
 *
 * RUN IT:
 *     java Java/05-methods/20-recursion.java
 *
 * Recursion is a method calling itself. The idea takes a minute. Using it well
 * means understanding what the CALL STACK is doing, and knowing the cases
 * where recursion is the wrong tool - which Section 4 measures.
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Recursion {

    /** Counts how deep the stack got before it overflowed, for Section 2. */
    static int depthReached = 0;

    /** Counts calls made by the naive Fibonacci, for Section 4. */
    static long naiveCallCount = 0;

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE SHAPE OF EVERY RECURSIVE METHOD
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - BASE CASE AND RECURSIVE CASE");
        System.out.println("=".repeat(74));

        System.out.println("  static int factorial(int n) {");
        System.out.println("      if (n <= 1) return 1;              // BASE CASE");
        System.out.println("      return n * factorial(n - 1);       // RECURSIVE CASE");
        System.out.println("  }");
        System.out.println();
        for (int n = 0; n <= 6; n++) {
            System.out.println("    factorial(" + n + ") = " + factorial(n));
        }

        System.out.println();
        System.out.println("  BOTH parts are mandatory, and so is one more thing: the");
        System.out.println("  recursive call must move TOWARD the base case. A method that");
        System.out.println("  calls factorial(n) instead of factorial(n - 1) has a perfectly");
        System.out.println("  good base case and still never terminates.");

        System.out.println();
        System.out.println("  THE THREE QUESTIONS to answer before writing any recursion:");
        System.out.println("    1. What is the smallest input I can answer immediately?");
        System.out.println("       -> that is your base case");
        System.out.println("    2. How do I make the problem smaller?");
        System.out.println("       -> that is your recursive step");
        System.out.println("    3. Does EVERY path reach the base case?");
        System.out.println("       -> that is termination");


        /* ====================================================================
         * SECTION 2 - THE CALL STACK, MADE VISIBLE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE CALL STACK");
        System.out.println("=".repeat(74));

        System.out.println("  Tracing factorial(4). Watch the frames pile up on the way");
        System.out.println("  DOWN, then unwind with actual values on the way back UP:");
        System.out.println();
        tracedFactorial(4, 0);

        System.out.println();
        System.out.println("  Nothing is COMPUTED until the base case is reached. The");
        System.out.println("  expression 4 * factorial(3) cannot multiply anything until");
        System.out.println("  factorial(3) returns - so all four frames exist at once.");

        /* --------------------------------------------------------------------
         * StackOverflowError. Deliberately provoked, and caught only so this
         * file can continue - you would never catch it in real code.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  WHAT HAPPENS WHEN YOU RUN OUT OF STACK:");

        depthReached = 0;
        try {
            infiniteRecursion();
        } catch (StackOverflowError e) {
            System.out.println("    StackOverflowError after " + String.format("%,d", depthReached)
                    + " frames");
        }
        System.out.println("    The stack is a FIXED size - typically 512 KB to 1 MB, which");
        System.out.println("    is roughly 10,000 to 20,000 frames for a simple method.");
        System.out.println();
        System.out.println("    Note it is an ERROR, not an Exception. It is caught here only");
        System.out.println("    so this file can continue. Never catch it in real code: it");
        System.out.println("    means a programming mistake, and the stack may be unusable.");
        System.out.println();
        System.out.println("    You CAN raise the limit with -Xss2m. If you need to, your");
        System.out.println("    algorithm is usually the thing that is wrong.");


        /* ====================================================================
         * SECTION 3 - JAVA HAS NO TAIL-CALL OPTIMISATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - NO TAIL-CALL OPTIMISATION IN JAVA");
        System.out.println("=".repeat(74));

        System.out.println("  This is a TAIL call - the recursive call is the very last");
        System.out.println("  thing the method does, so its frame is no longer needed:");
        System.out.println();
        System.out.println("    static int sumTo(int n, int accumulator) {");
        System.out.println("        if (n == 0) return accumulator;");
        System.out.println("        return sumTo(n - 1, accumulator + n);   // TAIL call");
        System.out.println("    }");
        System.out.println();
        System.out.println("  Scala, Kotlin (with tailrec), Haskell and Scheme all reuse that");
        System.out.println("  frame, turning the recursion into a constant-space loop.");
        System.out.println();
        System.out.println("  Java does NOT:");

        System.out.println("    sumTo(1_000)     = " + tailSum(1_000, 0));
        System.out.println("    sumTo(10_000)    = " + tailSum(10_000, 0));
        try {
            System.out.println("    sumTo(1_000_000) = " + tailSum(1_000_000, 0));
        } catch (StackOverflowError e) {
            System.out.println("    sumTo(1_000_000) -> StackOverflowError");
        }
        System.out.println("    The iterative equivalent handles it without blinking:");
        System.out.println("    iterativeSum(1_000_000) = " + iterativeSum(1_000_000));

        System.out.println();
        System.out.println("  THE PRACTICAL CONSEQUENCE: in Java, deep recursion must be");
        System.out.println("  converted to iteration BY HAND. There is no annotation that");
        System.out.println("  saves you. Section 7 shows the mechanical way to do it.");


        /* ====================================================================
         * SECTION 4 - THE FIBONACCI DISASTER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHY THE TEXTBOOK EXAMPLE IS A TRAP");
        System.out.println("=".repeat(74));

        System.out.println("  static long fib(int n) {");
        System.out.println("      if (n <= 1) return n;");
        System.out.println("      return fib(n - 1) + fib(n - 2);   // TWO calls per level");
        System.out.println("  }");
        System.out.println();
        System.out.println("  Two calls per level makes this O(2^n). Counting the calls:");
        System.out.println();
        System.out.printf("    %-8s %16s %14s%n", "n", "CALLS MADE", "TIME (ms)");

        for (int n : new int[]{10, 20, 30, 35, 40}) {
            naiveCallCount = 0;
            long start = System.nanoTime();
            naiveFib(n);
            long millis = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("    %-8d %,16d %14d%n", n, naiveCallCount, millis);
        }

        System.out.println();
        System.out.println("  Each step of 5 in n multiplies the work by roughly 11. The");
        System.out.println("  same subproblems are recomputed over and over:");
        System.out.println();
        System.out.println("                   fib(5)");
        System.out.println("              /            \\");
        System.out.println("          fib(4)            fib(3)");
        System.out.println("         /      \\          /      \\");
        System.out.println("     fib(3)    fib(2)   fib(2)   fib(1)");
        System.out.println("     /    \\");
        System.out.println("  fib(2) fib(1)          <- fib(2) computed three times already");

        System.out.println();
        System.out.println("  FIX 1 - MEMOISATION (top-down): cache each result once.");

        long startMemo = System.nanoTime();
        long memoResult = memoisedFib(50, new HashMap<>());
        long memoNanos = System.nanoTime() - startMemo;
        System.out.println("    memoisedFib(50)  = " + memoResult
                + "   in " + (memoNanos / 1000) + " microseconds");
        System.out.println();
        System.out.println("    O(2^n) collapses to O(n). Extrapolate the table above:");
        System.out.println("    each +5 in n costs about 11x more, so naive fib(50) would");
        System.out.println("    need roughly 40 BILLION calls - well over a minute. fib(70)");
        System.out.println("    would take weeks. The memoised version did it in microseconds.");

        System.out.println();
        System.out.println("  FIX 2 - ITERATION (bottom-up): no cache, no stack at all.");
        long startIter = System.nanoTime();
        long iterResult = iterativeFib(50);
        long iterNanos = System.nanoTime() - startIter;
        System.out.println("    iterativeFib(50) = " + iterResult
                + "   in " + (iterNanos / 1000) + " microseconds");
        System.out.println("    O(n) time, O(1) space, no stack risk. Ship this one.");

        System.out.println();
        System.out.println("  Both agree with the naive version where it can still finish:");
        System.out.println("    naive(30) = " + naiveFib(30)
                + ", memoised(30) = " + memoisedFib(30, new HashMap<>())
                + ", iterative(30) = " + iterativeFib(30));

        System.out.println();
        System.out.println("  THE GENERAL LESSON: a recursive solution that recomputes");
        System.out.println("  OVERLAPPING SUBPROBLEMS needs memoisation or a bottom-up");
        System.out.println("  rewrite. That single observation is the whole of dynamic");
        System.out.println("  programming.");


        /* ====================================================================
         * SECTION 5 - WHERE RECURSION GENUINELY WINS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - WHEN RECURSION IS THE RIGHT ANSWER");
        System.out.println("=".repeat(74));

        // Build a small binary search tree.
        Node tree = new Node(50,
                new Node(30, new Node(20, null, null), new Node(40, null, null)),
                new Node(70, new Node(60, null, null), new Node(80, null, null)));

        System.out.println("  TREE TRAVERSAL - the data is recursive, so the code should be.");
        System.out.println();
        System.out.println("            50");
        System.out.println("          /    \\");
        System.out.println("        30      70");
        System.out.println("       /  \\    /  \\");
        System.out.println("     20   40  60   80");
        System.out.println();

        List<Integer> inOrder = new ArrayList<>();
        collectInOrder(tree, inOrder);
        System.out.println("    in-order (recursive)  -> " + inOrder + "   sorted, for free");
        System.out.println("    height                -> " + height(tree));
        System.out.println("    sum of all nodes      -> " + sumTree(tree));
        System.out.println("    contains(40)          -> " + contains(tree, 40));
        System.out.println("    contains(45)          -> " + contains(tree, 45));
        System.out.println();
        System.out.println("    Each of those is three or four lines. The iterative versions");
        System.out.println("    need an explicit stack and are markedly harder to read.");

        System.out.println();
        System.out.println("  DIVIDE AND CONQUER - depth is O(log n), so the stack is safe.");
        int[] toSort = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("    before merge sort -> " + java.util.Arrays.toString(toSort));
        mergeSort(toSort, 0, toSort.length - 1);
        System.out.println("    after  merge sort -> " + java.util.Arrays.toString(toSort));
        System.out.println();
        System.out.println("    For a BILLION elements, merge sort recurses only about 30");
        System.out.println("    levels deep. Logarithmic depth is exactly why divide-and-");
        System.out.println("    conquer recursion is safe where linear recursion is not.");

        System.out.println();
        System.out.println("  BACKTRACKING - try a choice, recurse, undo if it fails.");
        List<String> permutations = new ArrayList<>();
        permute("", "abc", permutations);
        System.out.println("    permutations of \"abc\" -> " + permutations);
        System.out.println("    The 'undo' step is what makes an iterative version painful.");

        System.out.println();
        System.out.println("  Other natural fits: file system walking, JSON and XML parsing,");
        System.out.println("  nested expression evaluation, graph traversal, N-queens, sudoku.");


        /* ====================================================================
         * SECTION 6 - MUTUAL RECURSION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - MUTUAL (INDIRECT) RECURSION");
        System.out.println("=".repeat(74));

        System.out.println("  Methods that call EACH OTHER in a cycle:");
        System.out.println("    isEven(10) = " + isEven(10));
        System.out.println("    isOdd(7)   = " + isOdd(7));
        System.out.println("    isEven(3)  = " + isEven(3));
        System.out.println();
        System.out.println("  Silly here, but the real use is recursive-descent parsers:");
        System.out.println("    parseExpression -> parseTerm -> parseFactor -> parseExpression");
        System.out.println("  Just as prone to stack overflow as direct recursion:");
        try {
            isEven(1_000_000);
        } catch (StackOverflowError e) {
            System.out.println("    isEven(1_000_000) -> StackOverflowError");
        }


        /* ====================================================================
         * SECTION 7 - CONVERTING RECURSION TO ITERATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - CONVERTING TO ITERATION WITH AN EXPLICIT STACK");
        System.out.println("=".repeat(74));

        System.out.println("  Any recursion can be made iterative with an explicit stack,");
        System.out.println("  because that is exactly what the CALL stack was doing for you.");
        System.out.println();

        List<Integer> recursiveOrder = new ArrayList<>();
        collectPreOrder(tree, recursiveOrder);
        List<Integer> iterativeOrder = collectPreOrderIteratively(tree);

        System.out.println("    pre-order, recursive -> " + recursiveOrder);
        System.out.println("    pre-order, iterative -> " + iterativeOrder);
        System.out.println("    identical? " + recursiveOrder.equals(iterativeOrder));
        System.out.println();
        System.out.println("  Note in the source: the iterative version pushes RIGHT before");
        System.out.println("  LEFT, because a stack is last-in-first-out and we want left");
        System.out.println("  popped first. Getting that backwards is the classic bug.");
        System.out.println();
        System.out.println("  Use ArrayDeque, not the legacy Stack class (lesson 48).");
        System.out.println();
        System.out.println("  DO THIS CONVERSION when depth could exceed a few thousand:");
        System.out.println("    - parsing untrusted or user-supplied input");
        System.out.println("    - walking structures whose depth you do not control");
        System.out.println("    - traversing linked lists of unknown length");


        /* ====================================================================
         * SECTION 8 - RECURSION VS ITERATION, MEASURED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - THE COST OF A METHOD CALL");
        System.out.println("=".repeat(74));

        int iterations = 2_000_000;

        long startRecursive = System.nanoTime();
        long recursiveTotal = 0;
        for (int i = 0; i < iterations; i++) {
            recursiveTotal += factorial(15);
        }
        long recursiveMillis = (System.nanoTime() - startRecursive) / 1_000_000;

        long startIterative = System.nanoTime();
        long iterativeTotal = 0;
        for (int i = 0; i < iterations; i++) {
            iterativeTotal += iterativeFactorial(15);
        }
        long iterativeMillis = (System.nanoTime() - startIterative) / 1_000_000;

        System.out.printf("  factorial(15), %,d times:%n", iterations);
        System.out.println("    recursive : " + recursiveMillis + " ms");
        System.out.println("    iterative : " + iterativeMillis + " ms");
        System.out.println("    same result? " + (recursiveTotal == iterativeTotal));
        System.out.println();
        System.out.println("  SUMMARY OF THE TRADE-OFF:");
        System.out.printf("    %-14s %-22s %s%n", "", "RECURSION", "ITERATION");
        System.out.printf("    %-14s %-22s %s%n", "memory", "O(depth) stack frames", "O(1)");
        System.out.printf("    %-14s %-22s %s%n", "speed", "slower (call overhead)", "faster");
        System.out.printf("    %-14s %-22s %s%n", "failure mode", "StackOverflowError", "infinite loop");
        System.out.printf("    %-14s %-22s %s%n", "best for", "trees, divide+conquer", "linear work");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 20.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 1 AND 2 - FACTORIAL AND THE STACK
    // ------------------------------------------------------------------------

    /**
     * The canonical recursive factorial. Elegant, and O(n) in stack frames -
     * which is why {@link #iterativeFactorial(int)} is the one you would ship.
     *
     * @param n the number; must be non-negative
     * @return n factorial
     */
    static long factorial(int n) {
        if (n <= 1) {
            return 1;                      // BASE CASE
        }
        return n * factorial(n - 1);       // RECURSIVE CASE, moving toward the base
    }

    /**
     * The same computation with no recursion at all: O(1) stack, faster, and
     * incapable of overflowing.
     *
     * @param n the number; must be non-negative
     * @return n factorial
     */
    static long iterativeFactorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Factorial that narrates its own call stack, so the pile-up on the way
     * down and the unwind on the way up are both visible.
     *
     * @param n     the number
     * @param depth how deep this call is, used purely for indentation
     * @return n factorial
     */
    static long tracedFactorial(int n, int depth) {
        String indent = "    " + "  ".repeat(depth);
        System.out.println(indent + "-> factorial(" + n + ") called");

        if (n <= 1) {
            System.out.println(indent + "   BASE CASE, returning 1");
            return 1;
        }

        long fromBelow = tracedFactorial(n - 1, depth + 1);
        long result = n * fromBelow;
        System.out.println(indent + "<- factorial(" + n + ") returns " + n + " * "
                + fromBelow + " = " + result);
        return result;
    }

    /**
     * Deliberately unterminated recursion, used to provoke and measure a
     * StackOverflowError.
     */
    static void infiniteRecursion() {
        depthReached++;
        infiniteRecursion();   // no base case: the stack simply runs out
    }

    // ------------------------------------------------------------------------
    // SECTION 3 - TAIL CALLS
    // ------------------------------------------------------------------------

    /**
     * A tail-recursive sum. In a language with tail-call optimisation this
     * would use constant stack. In Java it does not.
     *
     * @param n           how many numbers remain to add
     * @param accumulator the running total carried down the recursion
     * @return the sum of 1..n plus the accumulator
     */
    static long tailSum(int n, long accumulator) {
        if (n == 0) {
            return accumulator;
        }
        return tailSum(n - 1, accumulator + n);   // the LAST thing this method does
    }

    /**
     * The same sum as a loop, which is what Java forces you to write by hand.
     *
     * @param n the upper bound
     * @return the sum of 1..n
     */
    static long iterativeSum(int n) {
        long total = 0;
        for (int i = 1; i <= n; i++) {
            total += i;
        }
        return total;
    }

    // ------------------------------------------------------------------------
    // SECTION 4 - FIBONACCI, THREE WAYS
    // ------------------------------------------------------------------------

    /**
     * The naive recursive Fibonacci: O(2^n), because it recomputes the same
     * subproblems exponentially many times.
     *
     * @param n which Fibonacci number to compute
     * @return the nth Fibonacci number
     */
    static long naiveFib(int n) {
        naiveCallCount++;
        if (n <= 1) {
            return n;
        }
        return naiveFib(n - 1) + naiveFib(n - 2);
    }

    /**
     * Memoised (top-down) Fibonacci: still recursive, but each value is
     * computed exactly once and cached. O(2^n) collapses to O(n).
     *
     * @param n     which Fibonacci number to compute
     * @param cache results computed so far
     * @return the nth Fibonacci number
     */
    static long memoisedFib(int n, Map<Integer, Long> cache) {
        if (n <= 1) {
            return n;
        }
        Long cached = cache.get(n);
        if (cached != null) {
            return cached;      // already solved - do not recurse again
        }
        long result = memoisedFib(n - 1, cache) + memoisedFib(n - 2, cache);
        cache.put(n, result);
        return result;
    }

    /**
     * Bottom-up Fibonacci: O(n) time, O(1) space, no recursion and no stack
     * risk. This is the version to ship.
     *
     * @param n which Fibonacci number to compute
     * @return the nth Fibonacci number
     */
    static long iterativeFib(int n) {
        if (n <= 1) {
            return n;
        }
        long previous = 0;
        long current = 1;
        for (int i = 2; i <= n; i++) {
            long next = previous + current;
            previous = current;
            current = next;
        }
        return current;
    }

    // ------------------------------------------------------------------------
    // SECTION 5 - TREES, SORTING AND BACKTRACKING
    // ------------------------------------------------------------------------

    /**
     * Walks the tree left-node-right, which visits a binary search tree in
     * sorted order. Three lines, because the data structure is recursive.
     *
     * @param node the subtree root, or null
     * @param into the list to collect values into
     */
    static void collectInOrder(Node node, List<Integer> into) {
        if (node == null) {
            return;                       // base case: an empty subtree
        }
        collectInOrder(node.left, into);
        into.add(node.value);
        collectInOrder(node.right, into);
    }

    /**
     * Walks the tree node-left-right.
     *
     * @param node the subtree root, or null
     * @param into the list to collect values into
     */
    static void collectPreOrder(Node node, List<Integer> into) {
        if (node == null) {
            return;
        }
        into.add(node.value);
        collectPreOrder(node.left, into);
        collectPreOrder(node.right, into);
    }

    /**
     * The same pre-order walk with an explicit stack instead of the call stack.
     * Note that right is pushed BEFORE left: a stack is last-in-first-out, so
     * pushing left last is what makes it pop first.
     *
     * @param root the tree root
     * @return the values in pre-order
     */
    static List<Integer> collectPreOrderIteratively(Node root) {
        List<Integer> result = new ArrayList<>();
        Deque<Node> stack = new ArrayDeque<>();
        if (root != null) {
            stack.push(root);
        }
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            result.add(node.value);
            if (node.right != null) stack.push(node.right);   // pushed first
            if (node.left != null) stack.push(node.left);     // popped first
        }
        return result;
    }

    /**
     * Measures the tree's height. The recursive definition ("one more than the
     * taller subtree") is almost the code itself.
     *
     * @param node the subtree root, or null
     * @return the height, where an empty tree is 0
     */
    static int height(Node node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(height(node.left), height(node.right));
    }

    /**
     * Sums every value in the tree.
     *
     * @param node the subtree root, or null
     * @return the total
     */
    static int sumTree(Node node) {
        if (node == null) {
            return 0;
        }
        return node.value + sumTree(node.left) + sumTree(node.right);
    }

    /**
     * Binary search over a search tree: at each step it discards half the
     * remaining nodes, so depth is O(log n).
     *
     * @param node   the subtree root, or null
     * @param target the value to look for
     * @return true if the value is present
     */
    static boolean contains(Node node, int target) {
        if (node == null) {
            return false;
        }
        if (node.value == target) {
            return true;
        }
        return target < node.value
                ? contains(node.left, target)
                : contains(node.right, target);
    }

    /**
     * Merge sort: split in half, sort each half, merge. Depth is O(log n), so
     * even a billion elements needs only about 30 stack frames.
     *
     * @param array the array to sort in place
     * @param low   the first index of the range
     * @param high  the last index of the range
     */
    static void mergeSort(int[] array, int low, int high) {
        if (low >= high) {
            return;                       // base case: 0 or 1 elements
        }
        int middle = low + (high - low) / 2;   // overflow-safe midpoint (lesson 03)
        mergeSort(array, low, middle);
        mergeSort(array, middle + 1, high);
        merge(array, low, middle, high);
    }

    /**
     * Merges two adjacent sorted ranges of the array back together.
     *
     * @param array  the array being sorted
     * @param low    the first index
     * @param middle the end of the first range
     * @param high   the last index
     */
    static void merge(int[] array, int low, int middle, int high) {
        int[] merged = new int[high - low + 1];
        int left = low;
        int right = middle + 1;
        int position = 0;

        while (left <= middle && right <= high) {
            merged[position++] = (array[left] <= array[right]) ? array[left++] : array[right++];
        }
        while (left <= middle) {
            merged[position++] = array[left++];
        }
        while (right <= high) {
            merged[position++] = array[right++];
        }
        System.arraycopy(merged, 0, array, low, merged.length);
    }

    /**
     * Generates every permutation by backtracking: take each remaining
     * character in turn, recurse on what is left, then move on. The "move on"
     * is the undo step that makes an iterative version awkward.
     *
     * @param prefix    the characters chosen so far
     * @param remaining the characters still available
     * @param into      the list to collect completed permutations into
     */
    static void permute(String prefix, String remaining, List<String> into) {
        if (remaining.isEmpty()) {
            into.add(prefix);             // base case: nothing left to choose
            return;
        }
        for (int i = 0; i < remaining.length(); i++) {
            permute(prefix + remaining.charAt(i),
                    remaining.substring(0, i) + remaining.substring(i + 1),
                    into);
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 6 - MUTUAL RECURSION
    // ------------------------------------------------------------------------

    /**
     * Determines evenness by asking whether n-1 is odd. Deliberately silly, but
     * a genuine example of mutual recursion.
     *
     * @param n a non-negative number
     * @return true if n is even
     */
    static boolean isEven(int n) {
        return n == 0 || isOdd(n - 1);
    }

    /**
     * The other half of the cycle.
     *
     * @param n a non-negative number
     * @return true if n is odd
     */
    static boolean isOdd(int n) {
        return n != 0 && isEven(n - 1);
    }
}

/**
 * A binary tree node. The class refers to itself, which is exactly why
 * recursive algorithms over it read so naturally.
 */
class Node {

    final int value;
    final Node left;
    final Node right;

    /**
     * @param value this node's value
     * @param left  the left subtree, or null
     * @param right the right subtree, or null
     */
    Node(int value, Node left, Node right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write `int power(int base, int exponent)` recursively. Then write the
 *    O(log n) version using the fact that x^n = (x^(n/2))^2 for even n. Count
 *    the calls in each for exponent = 1024.
 *
 * 2. Write `String reverse(String s)` recursively, then iteratively. At what
 *    input length does the recursive one overflow? Measure it.
 *
 * 3. Add memoisation to the permute() method... and then explain why you
 *    cannot, and what that tells you about which problems memoisation helps.
 *
 * 4. Implement `int countFiles(File dir)` recursively. Then break it with a
 *    symbolic link that points at its own parent, and add the guard that fixes
 *    it.
 *
 * 5. Convert collectInOrder() to an iterative version with an ArrayDeque. It
 *    is genuinely harder than the pre-order case - work out why before you
 *    start.
 *
 * 6. Solve the Towers of Hanoi for n discs recursively, printing each move.
 *    Then count the moves for n = 20 and explain why the count is 2^n - 1.
 * ============================================================================
 */
