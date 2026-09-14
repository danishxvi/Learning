/* ============================================================================
 * 49 - Comparable and Comparator
 * ----------------------------------------------------------------------------
 * Companion lesson: 49-comparable-and-comparator.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/49-comparable-and-comparator.java
 *
 * Lesson 46 showed a Comparator that silently dropped Bob from a TreeSet
 * because it only compared by age. Section 4 here reproduces the classic
 * compareTo() BUG that ships in real code to this day - subtraction that
 * overflows - and measures it actually happening.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ComparableAndComparator {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - Comparable: ONE NATURAL ORDER, OWNED BY THE CLASS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - Comparable: THE CLASS PICKS ITS OWN ORDER");
        System.out.println("=".repeat(74));

        List<Employee> employees = new ArrayList<>(List.of(
                new Employee("Dave", 45_000),
                new Employee("Alice", 72_000),
                new Employee("Carol", 58_000),
                new Employee("Bob", 72_000)
        ));

        System.out.println("    unsorted        -> " + employees);
        employees.sort(null);   // null Comparator -> use natural ordering, i.e. compareTo
        System.out.println("    sorted (natural)-> " + employees
                + "   <- Employee implements Comparable<Employee>, by salary");
        System.out.println();
        System.out.println("    Collections.sort(list) and list.sort(null) both mean THE SAME");
        System.out.println("    thing: use compareTo(). A class can only have ONE natural order -");
        System.out.println("    that is exactly why Comparator exists for everything else.");


        /* ====================================================================
         * SECTION 2 - Comparator: AS MANY ORDERS AS YOU NEED, FROM OUTSIDE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - Comparator: EXTERNAL, AND COMPOSABLE");
        System.out.println("=".repeat(74));

        List<Employee> byName = new ArrayList<>(employees);
        byName.sort(Comparator.comparing(Employee::name));
        System.out.println("    by name              -> " + byName);

        List<Employee> byNameReversed = new ArrayList<>(employees);
        byNameReversed.sort(Comparator.comparing(Employee::name).reversed());
        System.out.println("    by name, reversed()  -> " + byNameReversed);

        List<Employee> bySalaryThenName = new ArrayList<>(employees);
        bySalaryThenName.sort(
                Comparator.comparingInt(Employee::salary).thenComparing(Employee::name));
        System.out.println("    by salary, THEN name -> " + bySalaryThenName
                + "   <- Alice/Bob tie at 72,000, broken by name");
        System.out.println();
        System.out.println("    comparingInt/Long/Double avoid boxing an Integer/Long/Double");
        System.out.println("    per comparison the way comparing(Employee::salary) would if");
        System.out.println("    salary were boxed - a real, if usually small, allocation cost.");


        /* ====================================================================
         * SECTION 3 - SORT STABILITY: EQUAL ELEMENTS KEEP THEIR ORDER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - List.sort/Collections.sort IS STABLE");
        System.out.println("=".repeat(74));

        System.out.println("  \"Stable\" means: elements that compare EQUAL keep their ORIGINAL");
        System.out.println("  relative order. Arrays.sort/List.sort on OBJECTS uses TimSort, a");
        System.out.println("  stable merge sort. This is what makes multi-key sorting BY STAGES");
        System.out.println("  work at all - sort by the LEAST significant key first:");

        List<Employee> insertionOrder = new ArrayList<>(List.of(
                new Employee("Eve", 60_000),
                new Employee("Frank", 55_000),
                new Employee("Grace", 60_000),
                new Employee("Heidi", 55_000)
        ));
        System.out.println();
        System.out.println("    insertion order -> " + insertionOrder);

        List<Employee> stageSorted = new ArrayList<>(insertionOrder);
        stageSorted.sort(Comparator.comparing(Employee::name));       // stage 1: by name
        stageSorted.sort(Comparator.comparingInt(Employee::salary));  // stage 2: by salary
        System.out.println("    sorted by name, THEN stably by salary:");
        System.out.println("      " + stageSorted);
        System.out.println();
        System.out.println("    Within each salary, names stayed alphabetical - Frank before");
        System.out.println("    Heidi at 55,000, Eve before Grace at 60,000 - because the SECOND");
        System.out.println("    sort never disturbed the relative order of equal-salary elements");
        System.out.println("    that the FIRST sort had already established. A sort that were");
        System.out.println("    NOT stable could scramble this.");
        System.out.println();
        System.out.println("    (Arrays.sort on PRIMITIVE arrays - int[], double[], ... - uses");
        System.out.println("    dual-pivot quicksort instead, which is faster but NOT stable.");
        System.out.println("    Stability only matters for objects with independent keys, so");
        System.out.println("    this is not a contradiction - a primitive int has no 'other");
        System.out.println("    field' whose order could be disturbed.)");


        /* ====================================================================
         * SECTION 4 - THE COMPARETO OVERFLOW BUG: A REAL, DANGEROUS SHORTCUT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE compareTo SUBTRACTION BUG, REPRODUCED FOR REAL");
        System.out.println("=".repeat(74));

        System.out.println("  A shortcut that appears in real production code constantly:");
        System.out.println();
        System.out.println("      public int compareTo(Box other) {");
        System.out.println("          return this.value - other.value;   // \"clever\", and WRONG");
        System.out.println("      }");
        System.out.println();
        System.out.println("  It LOOKS correct - negative if smaller, positive if larger, zero");
        System.out.println("  if equal - and passes every casual test. It fails the moment the");
        System.out.println("  subtraction itself OVERFLOWS int's range.");

        BuggyBox big = new BuggyBox(Integer.MIN_VALUE);
        BuggyBox small = new BuggyBox(1);
        int buggyResult = big.compareTo(small);
        System.out.println();
        System.out.println("    BuggyBox(" + Integer.MIN_VALUE + ").compareTo(BuggyBox(1))");
        System.out.println("      MIN_VALUE - 1 wraps around to  -> " + buggyResult);
        System.out.println("      SIGN says                      -> " + (buggyResult < 0 ? "smaller" : "LARGER")
                + "   <- WRONG. " + Integer.MIN_VALUE + " really is smaller than 1.");

        CorrectBox correctBig = new CorrectBox(Integer.MIN_VALUE);
        CorrectBox correctSmall = new CorrectBox(1);
        int correctResult = correctBig.compareTo(correctSmall);
        System.out.println();
        System.out.println("    THE FIX - Integer.compare(a, b), which never overflows:");
        System.out.println("      CorrectBox(" + Integer.MIN_VALUE + ").compareTo(CorrectBox(1))");
        System.out.println("      result                          -> " + correctResult);
        System.out.println("      SIGN says                       -> " + (correctResult < 0 ? "smaller" : "larger")
                + "   <- correct");

        System.out.println();
        System.out.println("    A SORT BUILT ON THE BUGGY VERSION PRODUCES REAL, WRONG OUTPUT:");
        List<BuggyBox> buggyBoxes = new ArrayList<>(List.of(
                new BuggyBox(5), new BuggyBox(Integer.MIN_VALUE + 3), new BuggyBox(-1_000_000_000)));
        buggyBoxes.sort(BuggyBox::compareTo);
        System.out.println("      sorted with the subtraction bug -> " + buggyBoxes
                + "   (NOT actually ascending)");

        List<CorrectBox> correctBoxes = new ArrayList<>(List.of(
                new CorrectBox(5), new CorrectBox(Integer.MIN_VALUE + 3), new CorrectBox(-1_000_000_000)));
        correctBoxes.sort(CorrectBox::compareTo);
        System.out.println("      sorted with Integer.compare      -> " + correctBoxes + "   (genuinely ascending)");
        System.out.println();
        System.out.println("    ALWAYS use Integer.compare(a, b) / Long.compare / Double.compare");
        System.out.println("    (or comparingInt/Long/Double) - never subtract by hand.");


        /* ====================================================================
         * SECTION 5 - THE CONTRACT, AND THE CALLBACK TO LESSON 46's BUG
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE CONTRACT compareTo() MUST FOLLOW");
        System.out.println("=".repeat(74));

        System.out.println("    For any a, b, c:");
        System.out.println("      sgn(a.compareTo(b)) == -sgn(b.compareTo(a))      (antisymmetric)");
        System.out.println("      a.compareTo(b) > 0 && b.compareTo(c) > 0");
        System.out.println("        implies a.compareTo(c) > 0                    (transitive)");
        System.out.println("      a.compareTo(b) == 0 implies");
        System.out.println("        sgn(a.compareTo(c)) == sgn(b.compareTo(c))    (consistent ties)");
        System.out.println();
        System.out.println("    NOTHING in that contract requires compareTo() == 0 to imply");
        System.out.println("    equals() == true. That is legal, documented, and EXACTLY what");
        System.out.println("    lesson 46 Section 5 exploited: a TreeSet<Person> ordered only by");
        System.out.println("    age treated Alice(30) and Bob(30) as \"equal\" (compareTo == 0)");
        System.out.println("    even though equals() correctly said they were different people -");
        System.out.println("    and Bob was silently dropped. That was not a TreeSet bug. It was");
        System.out.println("    this exact, legal gap in the contract, in action.");
        System.out.println();
        System.out.println("    The JDK's own Javadoc for Comparable says this mismatch is");
        System.out.println("    \"strongly recommended (though not required)\" to avoid - now you");
        System.out.println("    have seen, concretely, WHY it is recommended.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 49.");
        System.out.println("=".repeat(74));
    }
}

/**
 * Implements {@code Comparable} - salary is the class's ONE natural order.
 */
class Employee implements Comparable<Employee> {
    private final String name;
    private final int salary;

    Employee(String name, int salary) {
        this.name = name;
        this.salary = salary;
    }

    String name() {
        return name;
    }

    int salary() {
        return salary;
    }

    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.salary, other.salary);
    }

    @Override
    public String toString() {
        return name + "(" + salary + ")";
    }
}

/**
 * The classic, WRONG {@code compareTo()} shortcut - subtraction overflows
 * for values far enough apart. See Section 4.
 */
class BuggyBox implements Comparable<BuggyBox> {
    final int value;

    BuggyBox(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(BuggyBox other) {
        return this.value - other.value;   // THE BUG
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

/**
 * The fix - {@code Integer.compare} never overflows, regardless of how far
 * apart the two values are.
 */
class CorrectBox implements Comparable<CorrectBox> {
    final int value;

    CorrectBox(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(CorrectBox other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write a Comparator<String> that orders strings by length, then
 *    alphabetically for ties. Sort {"bb", "a", "ccc", "dd", "e"} and predict
 *    the result before running it.
 *
 * 2. Find TWO more int values (besides MIN_VALUE and 1) that trigger the
 *    subtraction bug, and two that do NOT, to build intuition for when it
 *    actually bites.
 *
 * 3. Implement compareTo() for a Money class (long cents, String currency)
 *    that throws IllegalArgumentException when comparing different
 *    currencies - is that legal per the contract in Section 5? Why doesn't
 *    it violate transitivity?
 *
 * 4. Take Section 3's two-stage stable sort and combine it into ONE
 *    Comparator using thenComparing - confirm it produces the identical
 *    result in one pass instead of two.
 *
 * 5. Use Comparator.nullsFirst(Comparator.naturalOrder()) to sort a
 *    List<String> that contains a null element without it throwing.
 * ============================================================================
 */
