/* ============================================================================
 * 13 - MULTIDIMENSIONAL AND JAGGED ARRAYS
 * ----------------------------------------------------------------------------
 * Companion lesson: 13-multidimensional-arrays.md
 *
 * RUN IT:
 *     java Java/04-arrays-and-strings/13-multidimensional-arrays.java
 *
 * ONE FACT EXPLAINS EVERYTHING IN THIS LESSON:
 *   Java has no true 2D arrays. int[][] is an ARRAY OF REFERENCES to int[].
 * Every surprise below follows from that.
 * ============================================================================
 */

import java.util.Arrays;

class MultidimensionalArrays {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - int[][] IS AN ARRAY OF ARRAYS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - WHAT int[][] REALLY IS");
        System.out.println("=".repeat(74));

        int[][] grid = new int[3][4];

        System.out.println("  new int[3][4] created FOUR objects, not one:");
        System.out.println("    the outer array (3 references) plus 3 separate int[4] rows");
        System.out.println();
        System.out.println("  grid.length       = " + grid.length + "   <- number of ROWS");
        System.out.println("  grid[0].length    = " + grid[0].length + "   <- length of row 0");
        System.out.println();
        System.out.println("  Each row is a REAL, INDEPENDENT object you can point at:");
        System.out.println("    grid[0] -> " + grid[0]);
        System.out.println("    grid[1] -> " + grid[1]);
        System.out.println("    grid[2] -> " + grid[2]);
        System.out.println("  Three different identity hashes: three separate int[4] objects.");

        // Because a row is a real int[], you can hand it to anything expecting one.
        int[] extractedRow = grid[1];
        extractedRow[0] = 42;
        System.out.println();
        System.out.println("  int[] row = grid[1];  row[0] = 42;");
        System.out.println("    grid[1] is now " + Arrays.toString(grid[1])
                + "   <- the SAME object, not a copy");

        System.out.println();
        System.out.println("  In C, int grid[3][4] is ONE flat contiguous block. In Java it");
        System.out.println("  is four separate heap objects that may sit far apart in memory.");
        System.out.println("  Section 5 shows why that difference is measurable.");


        /* ====================================================================
         * SECTION 2 - THE WAYS TO CREATE ONE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - CREATING MULTIDIMENSIONAL ARRAYS");
        System.out.println("=".repeat(74));

        int[][] rectangular = new int[2][3];
        System.out.println("  new int[2][3]           -> " + Arrays.deepToString(rectangular));

        int[][] withValues = {
                {1, 2, 3},
                {4, 5, 6}
        };
        System.out.println("  literal initialiser     -> " + Arrays.deepToString(withValues));

        int[][][] cube = new int[2][2][2];
        System.out.println("  new int[2][2][2]        -> " + Arrays.deepToString(cube));

        /* --------------------------------------------------------------------
         * new int[3][] allocates the OUTER array only. Every row is null until
         * you create it. This is the most common multidimensional array bug.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("new int[3][] leaves the ROWS null:");

        int[][] rowsNotYetMade = new int[3][];
        System.out.println("  rowsNotYetMade[0]       -> " + rowsNotYetMade[0]);

        try {
            System.out.println(rowsNotYetMade[0].length);
        } catch (NullPointerException e) {
            System.out.println("  rowsNotYetMade[0].length -> NullPointerException");
            System.out.println("  You must allocate each row before using it.");
        }

        for (int i = 0; i < rowsNotYetMade.length; i++) {
            rowsNotYetMade[i] = new int[2];
        }
        System.out.println("  after allocating each row -> " + Arrays.deepToString(rowsNotYetMade));

        System.out.println();
        System.out.println("  int[][] a = new int[][4];   -> COMPILE ERROR");
        System.out.println("  You cannot give a later dimension without the earlier one:");
        System.out.println("  there would be nothing to hold the rows.");


        /* ====================================================================
         * SECTION 3 - JAGGED ARRAYS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - ROWS MAY HAVE DIFFERENT LENGTHS");
        System.out.println("=".repeat(74));

        // Because each row is a separate object, nothing requires them to match.
        int[][] triangle = new int[5][];
        for (int i = 0; i < triangle.length; i++) {
            triangle[i] = new int[i + 1];
            Arrays.fill(triangle[i], i + 1);
        }

        System.out.println("A jagged (triangular) array:");
        for (int[] row : triangle) {
            System.out.println("    length " + row.length + ": " + Arrays.toString(row));
        }

        System.out.println();
        System.out.println("Pascal's triangle, which needs jaggedness to be natural:");
        int[][] pascal = buildPascalsTriangle(7);
        for (int[] row : pascal) {
            // Indent each row so the triangle is centred. Note we use repeat()
            // rather than a computed printf width: "%0s" is not a valid
            // specifier, so a zero-width pad would throw on the widest row.
            System.out.print("   " + " ".repeat(2 * (pascal.length - row.length)));
            for (int value : row) {
                System.out.printf("%4d", value);
            }
            System.out.println();
        }

        /* --------------------------------------------------------------------
         * NEVER ASSUME RECTANGULARITY. Use grid[i].length, not grid[0].length.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("THE JAGGED-ARRAY BUG - using grid[0].length as the bound:");

        int[][] jagged = {
                {1},
                {2, 3},
                {4, 5, 6}
        };

        System.out.print("  with grid[0].length (WRONG): ");
        try {
            for (int i = 0; i < jagged.length; i++) {
                for (int j = 0; j < jagged[0].length; j++) {   // row 0 has length 1
                    System.out.print(jagged[i][j] + " ");
                }
            }
            System.out.println("  <- silently visited only the first column");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ArrayIndexOutOfBoundsException");
        }

        System.out.print("  with grid[i].length (RIGHT): ");
        for (int i = 0; i < jagged.length; i++) {
            for (int j = 0; j < jagged[i].length; j++) {
                System.out.print(jagged[i][j] + " ");
            }
        }
        System.out.println();

        System.out.print("  with the enhanced for      : ");
        for (int[] row : jagged) {
            for (int value : row) {
                System.out.print(value + " ");
            }
        }
        System.out.println("  <- handles jaggedness automatically");


        /* ====================================================================
         * SECTION 4 - PRINTING AND COMPARING NEED THE deep VERSIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - USE THE deep METHODS");
        System.out.println("=".repeat(74));

        int[][] left = {{1, 2}, {3, 4}};
        int[][] right = {{1, 2}, {3, 4}};

        System.out.println("Printing:");
        System.out.println("  System.out.println(grid)        -> " + left);
        System.out.println("  Arrays.toString(grid)           -> " + Arrays.toString(left));
        System.out.println("    ^ still useless: toString() was called on each ROW, and a");
        System.out.println("      row is itself an array with the default toString().");
        System.out.println("  Arrays.deepToString(grid)       -> " + Arrays.deepToString(left)
                + "   <- correct");

        System.out.println();
        System.out.println("Comparing two 2D arrays with identical contents:");
        System.out.println("  left == right                   -> " + (left == right));
        System.out.println("  Arrays.equals(left, right)      -> " + Arrays.equals(left, right)
                + "   <- compares row REFERENCES");
        System.out.println("  Arrays.deepEquals(left, right)  -> " + Arrays.deepEquals(left, right)
                + "    <- correct");

        System.out.println();
        System.out.println("Hashing follows the same pattern:");
        System.out.println("  Arrays.hashCode(left)     = " + Arrays.hashCode(left));
        System.out.println("  Arrays.hashCode(right)    = " + Arrays.hashCode(right)
                + "   <- different, despite equal contents");
        System.out.println("  Arrays.deepHashCode(left) = " + Arrays.deepHashCode(left));
        System.out.println("  Arrays.deepHashCode(right)= " + Arrays.deepHashCode(right)
                + "   <- equal, as it should be");


        /* ====================================================================
         * SECTION 5 - COPYING IS SHALLOW BY DEFAULT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - clone() ON A 2D ARRAY SHARES THE ROWS");
        System.out.println("=".repeat(74));

        int[][] source = {{1, 2}, {3, 4}};
        int[][] shallowCopy = source.clone();

        shallowCopy[0][0] = 99;

        System.out.println("  int[][] copy = source.clone();  copy[0][0] = 99;");
        System.out.println("    source -> " + Arrays.deepToString(source) + "   <- also changed!");
        System.out.println("    copy   -> " + Arrays.deepToString(shallowCopy));
        System.out.println("    source == copy         -> " + (source == shallowCopy)
                + "   (different outer arrays)");
        System.out.println("    source[0] == copy[0]   -> " + (source[0] == shallowCopy[0])
                + "    <- but the SAME row object");

        // A real deep copy: clone every row.
        int[][] freshSource = {{1, 2}, {3, 4}};
        int[][] deepCopy = new int[freshSource.length][];
        for (int i = 0; i < freshSource.length; i++) {
            deepCopy[i] = freshSource[i].clone();
        }
        deepCopy[0][0] = 99;

        System.out.println();
        System.out.println("  A real deep copy clones every ROW:");
        System.out.println("    source -> " + Arrays.deepToString(freshSource) + "    <- untouched");
        System.out.println("    copy   -> " + Arrays.deepToString(deepCopy));

        // The same thing as a one-liner with streams (lesson 54).
        int[][] streamCopy = Arrays.stream(freshSource)
                .map(int[]::clone)
                .toArray(int[][]::new);
        streamCopy[1][1] = 77;
        System.out.println();
        System.out.println("  As a stream one-liner:");
        System.out.println("    Arrays.stream(a).map(int[]::clone).toArray(int[][]::new)");
        System.out.println("    source -> " + Arrays.deepToString(freshSource) + "    <- still untouched");
        System.out.println("    copy   -> " + Arrays.deepToString(streamCopy));


        /* ====================================================================
         * SECTION 6 - ROW-MAJOR ITERATION IS FASTER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - LOOP ORDER MATTERS, MEASURABLY");
        System.out.println("=".repeat(74));

        int size = 4000;
        int[][] big = new int[size][size];

        System.out.println("  A " + size + " x " + size + " int array ("
                + String.format("%,d", (long) size * size) + " elements).");
        System.out.println("  Identical logic, identical result, different loop order.");
        System.out.println("  Two rounds, because the first pays for JIT warm-up:");
        System.out.println();
        System.out.printf("    %-8s %16s %16s%n", "ROUND", "ROW-MAJOR", "COLUMN-MAJOR");

        for (int round = 0; round < 2; round++) {
            long rowMajorMillis = timeRowMajor(big);
            long columnMajorMillis = timeColumnMajor(big);
            System.out.printf("    %-8d %13d ms %13d ms%n", round, rowMajorMillis, columnMajorMillis);
        }

        System.out.println();
        System.out.println("  WHY: when you read big[i][0], the CPU loads a whole CACHE LINE");
        System.out.println("  (typically 64 bytes = 16 ints) into fast memory. Reading");
        System.out.println("  big[i][1]..big[i][15] is then nearly free - already there.");
        System.out.println();
        System.out.println("  Column-major reads ONE int from each cache line and then jumps");
        System.out.println("  to a completely different row object, so almost every access is");
        System.out.println("  a cache miss.");
        System.out.println();
        System.out.println("  This is one of the few micro-optimisations worth knowing,");
        System.out.println("  because writing the loops in the right order costs nothing.");


        /* ====================================================================
         * SECTION 7 - COMMON PATTERNS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - PATTERNS YOU WILL ACTUALLY USE");
        System.out.println("=".repeat(74));

        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6}
        };

        System.out.println("Original (2 x 3):");
        printMatrix(matrix);

        System.out.println();
        System.out.println("Transposed (3 x 2):");
        printMatrix(transpose(matrix));

        System.out.println();
        System.out.println("Row sums:");
        for (int i = 0; i < matrix.length; i++) {
            System.out.println("    row " + i + " -> " + Arrays.stream(matrix[i]).sum());
        }

        System.out.println();
        System.out.println("Filling a grid of objects - fill() works PER ROW:");
        String[][] board = new String[3][3];
        for (String[] row : board) {
            Arrays.fill(row, ".");
        }
        board[1][1] = "X";
        for (String[] row : board) {
            System.out.println("    " + String.join(" ", row));
        }
        System.out.println("  Arrays.fill(board, \".\") would NOT compile: the outer array");
        System.out.println("  holds String[] elements, not String.");


        /* ====================================================================
         * SECTION 8 - WHEN NOT TO USE A 2D ARRAY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - OFTEN THE WRONG MODEL");
        System.out.println("=".repeat(74));

        System.out.println("  String[][] of records      -> use a List<Person> and a class");
        System.out.println("  int[][] used as a lookup    -> use a Map<Key, Value>");
        System.out.println("  a sparse grid, mostly zeros -> use a Map<Point, Value>");
        System.out.println("  serious matrix maths        -> use a library");
        System.out.println();
        System.out.println("For heavy numeric work, a FLAT array with computed indices is");
        System.out.println("faster, because it is genuinely contiguous:");
        System.out.println();

        int rows = 3, cols = 4;
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = i * 10 + j;    // manual row-major indexing
            }
        }
        System.out.println("    double[] flat = new double[rows * cols];");
        System.out.println("    flat[i * cols + j] = value;");
        System.out.println("    -> " + Arrays.toString(flat));
        System.out.println();
        System.out.println("  One object, one contiguous block, no reference chasing.");
        System.out.println("  This is exactly what numerical libraries do internally, to");
        System.out.println("  get the contiguity Java's [][] does not give them.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 13.");
        System.out.println("=".repeat(74));
    }

    /**
     * Builds Pascal's triangle, where each row is one element longer than the
     * last - a problem that is natural with jagged arrays and awkward without.
     *
     * @param height how many rows to build
     * @return a jagged array of binomial coefficients
     */
    static int[][] buildPascalsTriangle(int height) {
        int[][] triangle = new int[height][];
        for (int row = 0; row < height; row++) {
            triangle[row] = new int[row + 1];
            triangle[row][0] = 1;
            triangle[row][row] = 1;
            for (int col = 1; col < row; col++) {
                triangle[row][col] = triangle[row - 1][col - 1] + triangle[row - 1][col];
            }
        }
        return triangle;
    }

    /**
     * Returns a new array with rows and columns swapped. Note the result's
     * dimensions are the reverse of the input's.
     *
     * @param source a rectangular 2D array
     * @return its transpose
     */
    static int[][] transpose(int[][] source) {
        int[][] result = new int[source[0].length][source.length];
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[i].length; j++) {
                result[j][i] = source[i][j];
            }
        }
        return result;
    }

    /**
     * Sums every element walking rows first - the cache-friendly order.
     *
     * @param grid the array to traverse
     * @return elapsed milliseconds
     */
    static long timeRowMajor(int[][] grid) {
        long start = System.nanoTime();
        long total = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                total += grid[i][j];
            }
        }
        long elapsed = System.nanoTime() - start;
        if (total == Long.MIN_VALUE) System.out.print("");   // keep the loop alive
        return elapsed / 1_000_000;
    }

    /**
     * Sums every element walking columns first - the same work, but jumping to
     * a different row object on every single access.
     *
     * @param grid the array to traverse
     * @return elapsed milliseconds
     */
    static long timeColumnMajor(int[][] grid) {
        long start = System.nanoTime();
        long total = 0;
        for (int j = 0; j < grid[0].length; j++) {
            for (int i = 0; i < grid.length; i++) {
                total += grid[i][j];
            }
        }
        long elapsed = System.nanoTime() - start;
        if (total == Long.MIN_VALUE) System.out.print("");
        return elapsed / 1_000_000;
    }

    /**
     * Prints a 2D int array as an aligned grid.
     *
     * @param matrix the array to print
     */
    static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.print("   ");
            for (int value : row) {
                System.out.printf("%5d", value);
            }
            System.out.println();
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write `int[][] identity(int n)` returning an n x n matrix with 1s on the
 *    diagonal and 0s elsewhere. Print it with deepToString.
 *
 * 2. Write `int[][] multiply(int[][] a, int[][] b)`. Validate that a's column
 *    count equals b's row count, and throw IllegalArgumentException if not.
 *
 * 3. Rotate a square matrix 90 degrees clockwise, IN PLACE (no second array).
 *    Hint: transpose, then reverse each row.
 *
 * 4. Build a jagged array where row i has i*i elements, for i in 0..5. Print
 *    each row's length. Then sum every element with the enhanced for.
 *
 * 5. Take the Section 6 benchmark and change `size` to 500, then 8000. Note
 *    that at small sizes the whole array fits in cache and the gap disappears.
 *    That is the clearest possible proof of what is causing it.
 *
 * 6. Implement a tic-tac-toe board as String[3][3] with a winner check that
 *    handles rows, columns and both diagonals. Then argue for or against
 *    modelling it as a single String[9] instead.
 * ============================================================================
 */
