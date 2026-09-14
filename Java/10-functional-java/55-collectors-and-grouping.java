/* ============================================================================
 * 55 - Collectors, grouping and partitioning
 * ----------------------------------------------------------------------------
 * Companion lesson: 55-collectors-and-grouping.md
 *
 * RUN IT:
 *     java Java/10-functional-java/55-collectors-and-grouping.java
 *
 * Section 2 catches a real gotcha: Collectors.toList() and stream.toList()
 * LOOK the same and behave DIFFERENTLY - one is mutable, one throws.
 * Section 5 reproduces toMap's real exception on duplicate keys.
 * ============================================================================
 */

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CollectorsAndGrouping {

    record Employee(String name, String department, int salary) {
    }

    public static void main(String[] args) {

        List<Employee> employees = List.of(
                new Employee("Alice", "Engineering", 95_000),
                new Employee("Bob", "Engineering", 82_000),
                new Employee("Carol", "Sales", 70_000),
                new Employee("Dave", "Sales", 65_000),
                new Employee("Eve", "Engineering", 110_000),
                new Employee("Frank", "Marketing", 60_000)
        );

        /* ====================================================================
         * SECTION 1 - toList, toSet, joining
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - toList, toSet, joining");
        System.out.println("=".repeat(74));

        List<String> names = employees.stream().map(Employee::name).collect(Collectors.toList());
        System.out.println("    Collectors.toList()   -> " + names);

        java.util.Set<String> departments = employees.stream()
                .map(Employee::department).collect(Collectors.toSet());
        System.out.println("    Collectors.toSet()    -> " + departments + "   (dedup'd - Set semantics)");

        String joined = employees.stream().map(Employee::name)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("    Collectors.joining    -> " + joined);


        /* ====================================================================
         * SECTION 2 - THE REAL GOTCHA: Collectors.toList() vs stream.toList()
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THESE LOOK THE SAME AND ARE NOT");
        System.out.println("=".repeat(74));

        List<Integer> viaCollectors = Stream.of(1, 2, 3).collect(Collectors.toList());
        List<Integer> viaStreamMethod = Stream.of(1, 2, 3).toList();

        System.out.println("    Collectors.toList()  -> " + viaCollectors);
        System.out.println("    stream.toList()       -> " + viaStreamMethod
                + "   (Java 16+ instance method, NOT the same as the line above)");

        viaCollectors.add(4);
        System.out.println();
        System.out.println("    viaCollectors.add(4)  -> succeeded, now " + viaCollectors);
        System.out.println("      Collectors.toList()'s CONTRACT never actually promises");
        System.out.println("      mutability OR immutability - the current implementation just");
        System.out.println("      happens to return a plain, mutable ArrayList.");

        try {
            viaStreamMethod.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println();
            System.out.println("    viaStreamMethod.add(4) -> UnsupportedOperationException");
            System.out.println("      stream.toList() EXPLICITLY guarantees an unmodifiable list -");
            System.out.println("      a REAL, contractual difference from Collectors.toList(), not");
            System.out.println("      an accident of implementation. Relying on Collectors.toList()");
            System.out.println("      being mutable is relying on unspecified behavior.");
        }


        /* ====================================================================
         * SECTION 3 - groupingBy: THE WORKHORSE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - groupingBy: ONE KEY, MULTIPLE BUCKETS");
        System.out.println("=".repeat(74));

        Map<String, List<Employee>> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department));
        System.out.println("    groupingBy(Employee::department) ->");
        byDepartment.forEach((dept, list) -> System.out.println("      " + dept + " -> " + list));

        System.out.println();
        System.out.println("    WITH A DOWNSTREAM COLLECTOR - groupingBy's SECOND argument");
        System.out.println("    processes each GROUP further, instead of just collecting a List:");

        Map<String, Long> countByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
        System.out.println("      groupingBy(dept, counting())        -> " + countByDepartment);

        Map<String, Double> avgSalaryByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.averagingInt(Employee::salary)));
        System.out.println("      groupingBy(dept, averagingInt(sal)) -> " + avgSalaryByDepartment);

        Map<String, List<String>> namesByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.mapping(Employee::name, Collectors.toList())));
        System.out.println("      groupingBy(dept, mapping(name, toList())) -> " + namesByDepartment);

        System.out.println();
        System.out.println("    MULTI-LEVEL grouping - groupingBy INSIDE groupingBy:");
        Map<String, Map<Boolean, List<Employee>>> byDeptThenHighEarner = employees.stream()
                .collect(Collectors.groupingBy(Employee::department,
                        Collectors.groupingBy(e -> e.salary() > 80_000)));
        byDeptThenHighEarner.forEach((dept, subMap) ->
                System.out.println("      " + dept + " -> " + subMap));
        System.out.println();
        System.out.println("    NOTICE: Engineering's inner map has ONLY a \"true\" key - every");
        System.out.println("    Engineering employee earns over 80,000, so there IS no \"false\"");
        System.out.println("    group, and nested groupingBy simply OMITS it. Section 4's");
        System.out.println("    partitioningBy would keep an explicit, empty \"false\" key instead -");
        System.out.println("    that is the real, concrete difference between the two.");


        /* ====================================================================
         * SECTION 4 - partitioningBy: EXACTLY TWO GROUPS, ALWAYS BOTH PRESENT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - partitioningBy: A SPECIAL CASE OF groupingBy");
        System.out.println("=".repeat(74));

        Map<Boolean, List<Employee>> partitioned = employees.stream()
                .collect(Collectors.partitioningBy(e -> e.salary() > 80_000));
        System.out.println("    partitioningBy(salary > 80,000):");
        System.out.println("      true  (high earners) -> " + partitioned.get(true));
        System.out.println("      false (everyone else) -> " + partitioned.get(false));
        System.out.println();
        System.out.println("    THE REAL DIFFERENCE FROM groupingBy(Predicate): partitioningBy's");
        System.out.println("    result ALWAYS has BOTH true and false keys present, even if one");
        System.out.println("    side is EMPTY - groupingBy would simply omit an empty group's key");
        System.out.println("    entirely. Use partitioningBy specifically when the CALLER always");
        System.out.println("    needs to handle both branches without a null/missing-key check.");


        /* ====================================================================
         * SECTION 5 - toMap: POWERFUL, AND A REAL EXCEPTION ON DUPLICATE KEYS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - toMap: THE DUPLICATE-KEY TRAP, REPRODUCED");
        System.out.println("=".repeat(74));

        Map<String, Integer> nameToSalary = employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary));
        System.out.println("    toMap(name, salary) with UNIQUE keys -> " + nameToSalary);

        System.out.println();
        System.out.println("    toMap(department, salary) - department is NOT unique, three");
        System.out.println("    employees share \"Engineering\":");
        try {
            employees.stream().collect(Collectors.toMap(Employee::department, Employee::salary));
        } catch (IllegalStateException e) {
            System.out.println("      -> IllegalStateException: " + e.getMessage());
        }

        System.out.println();
        System.out.println("    THE FIX - toMap's THIRD argument, a MERGE function for collisions:");
        Map<String, Integer> maxSalaryByDept = employees.stream()
                .collect(Collectors.toMap(Employee::department, Employee::salary, Integer::max));
        System.out.println("      toMap(dept, salary, Integer::max) -> " + maxSalaryByDept);


        /* ====================================================================
         * SECTION 6 - BUILDING A CUSTOM Collector
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - A CUSTOM Collector, FROM SCRATCH");
        System.out.println("=".repeat(74));

        System.out.println("    Collector.of() takes FOUR pieces: a supplier (new container), an");
        System.out.println("    accumulator (add one element), a combiner (merge two containers -");
        System.out.println("    for parallel streams, same role as reduce's combiner in lesson 54),");
        System.out.println("    and a finisher (container -> final result):");

        java.util.stream.Collector<Employee, ?, String> nameSummary = java.util.stream.Collector.of(
                StringBuilder::new,
                (sb, e) -> sb.append(sb.isEmpty() ? "" : ", ").append(e.name()),
                (sb1, sb2) -> sb1.append(sb1.isEmpty() ? "" : ", ").append(sb2),
                StringBuilder::toString
        );
        String summary = employees.stream().collect(nameSummary);
        System.out.println("      custom Collector building a comma list -> " + summary);


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 55.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Using the same Employee list, group by department, then within each
 *    department find the highest-paid employee (Collectors.maxBy as a
 *    downstream collector).
 *
 * 2. Reproduce Section 5's IllegalStateException, then fix it with a merge
 *    function that CONCATENATES colliding names instead of picking a max.
 *
 * 3. Use partitioningBy to split a List<Integer> into evens and odds, then
 *    print BOTH groups even when one is empty (use a list that is all
 *    even numbers to see partitioningBy still produce a `false -> []` key).
 *
 * 4. Write a downstream collector chain that produces
 *    Map<String, IntSummaryStatistics> (Collectors.summarizingInt) for
 *    salary, grouped by department - print min/max/average from it.
 *
 * 5. Extend Section 6's custom Collector to also track a running COUNT
 *    alongside the joined names, returning a record with both fields.
 * ============================================================================
 */
