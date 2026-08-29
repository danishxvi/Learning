/* ============================================================================
 * 43 - GENERICS, WILDCARDS AND TYPE ERASURE
 * ----------------------------------------------------------------------------
 * Companion lesson: 43-generics.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/43-generics.java
 *
 * Generics move type errors from RUNTIME to COMPILE TIME. Everything strange
 * about them follows from one implementation decision: ERASURE (Section 3).
 * ============================================================================
 */

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

class Generics {

    public static void main(String[] args) throws Exception {

        /* ====================================================================
         * SECTION 1 - THE PROBLEM THEY SOLVE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - MOVING THE ERROR FROM RUNTIME TO COMPILE TIME");
        System.out.println("=".repeat(74));

        System.out.println("  BEFORE JAVA 5 - a raw list accepts anything:");

        @SuppressWarnings("rawtypes")
        List rawList = new ArrayList();
        rawList.add("Danish");
        rawList.add(42);                       // no complaint at all

        System.out.println("    rawList.add(\"Danish\");  rawList.add(42);   -> both accepted");
        System.out.println("    contents: " + rawList);
        try {
            String value = (String) rawList.get(1);
            System.out.println("    unreachable: " + value);
        } catch (ClassCastException e) {
            System.out.println("    (String) rawList.get(1) -> ClassCastException AT RUNTIME");
            System.out.println("      " + e.getMessage());
        }

        System.out.println();
        System.out.println("  WITH GENERICS - the same mistake is a COMPILE error:");
        List<String> names = new ArrayList<>();
        names.add("Danish");
        // names.add(42);
        //   ERROR: incompatible types: int cannot be converted to String
        String name = names.get(0);            // no cast needed
        System.out.println("    names.add(42);          -> COMPILE ERROR");
        System.out.println("    String s = names.get(0) -> no cast needed: " + name);
        System.out.println();
        System.out.println("  The failure moved from a customer's screen to your editor.");


        /* ====================================================================
         * SECTION 2 - GENERIC CLASSES, METHODS AND BOUNDS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - WRITING YOUR OWN");
        System.out.println("=".repeat(74));

        Box<String> stringBox = new Box<>();       // the diamond infers <String>
        stringBox.put("a string");
        Box<Integer> intBox = new Box<>();
        intBox.put(42);

        System.out.println("    Box<String>  -> " + stringBox.get()
                + "   (returned as a String, no cast)");
        System.out.println("    Box<Integer> -> " + intBox.get());

        System.out.println();
        System.out.println("  GENERIC METHODS put the parameter BEFORE the return type:");
        System.out.println("      static <T> void printAll(List<T> items)");
        System.out.println("      static <T, R> List<R> map(List<T> src, Function<T, R> f)");
        System.out.println();
        System.out.println("    map(List.of(1,2,3), n -> n * 10) -> "
                + map(List.of(1, 2, 3), n -> n * 10));
        System.out.println("    map(List.of(\"a\",\"bb\"), String::length) -> "
                + map(List.of("a", "bb"), String::length));
        System.out.println("    Usually inferred. Occasionally explicit:");
        System.out.println("      Collections.<String>emptyList()");

        System.out.println();
        System.out.println("  CONVENTIONAL NAMES:");
        System.out.printf("    %-6s %-12s %-6s %-12s %-6s %s%n",
                "T", "type", "E", "element", "K", "key");
        System.out.printf("    %-6s %-12s %-6s %-12s %-6s %s%n",
                "V", "value", "R", "result", "N", "number");

        System.out.println();
        System.out.println("  BOUNDS are what let you CALL METHODS on T:");
        System.out.println("      static <T extends Number> double sum(List<T> numbers)");
        System.out.println("    sum(List.of(1, 2, 3))       -> " + sum(List.of(1, 2, 3)));
        System.out.println("    sum(List.of(1.5, 2.5))      -> " + sum(List.of(1.5, 2.5)));
        System.out.println();
        System.out.println("    Without the bound, T erases to Object and you could only");
        System.out.println("    call Object's methods - no doubleValue().");
        System.out.println();
        System.out.println("  MULTIPLE BOUNDS use &, class first if there is one:");
        System.out.println("      <T extends Comparable<T> & Serializable>");
        System.out.println("    largest(List.of(3, 9, 2)) -> " + largest(List.of(3, 9, 2)));
        System.out.println("    largest(List.of(\"b\",\"z\",\"a\")) -> "
                + largest(List.of("b", "z", "a")));


        /* ====================================================================
         * SECTION 3 - ERASURE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE FACT THAT EXPLAINS EVERYTHING ELSE");
        System.out.println("=".repeat(74));

        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        System.out.println("  GENERICS EXIST ONLY AT COMPILE TIME. The compiler checks types,");
        System.out.println("  inserts casts, and then ERASES the parameters:");
        System.out.println();
        System.out.println("    new ArrayList<String>().getClass()  -> " + strings.getClass().getName());
        System.out.println("    new ArrayList<Integer>().getClass() -> " + integers.getClass().getName());
        System.out.println("    same class? " + (strings.getClass() == integers.getClass())
                + "   <- both are just ArrayList at runtime");

        System.out.println();
        System.out.println("  What each thing erases TO:");
        System.out.printf("    %-32s %s%n", "List<String>", "List");
        System.out.printf("    %-32s %s%n", "T", "Object");
        System.out.printf("    %-32s %s%n", "T extends Number", "Number");
        System.out.printf("    %-32s %s%n", "T extends Comparable<T> & Serializable", "Comparable");

        // Reflection can still see the DECLARED generic signature, because it
        // is kept in the class file's metadata - just not in the bytecode of
        // the method bodies.
        Method sumMethod = Generics.class.getDeclaredMethod("sum", List.class);
        System.out.println();
        System.out.println("  Reflection can still read the DECLARED signature from metadata:");
        System.out.println("    sum's parameter -> " + sumMethod.getGenericParameterTypes()[0]);
        System.out.println("    ...but that is documentation, not a runtime check.");

        System.out.println();
        System.out.println("  WHY ERASURE? BACKWARD COMPATIBILITY. Java 5 had to let generic");
        System.out.println("  and pre-generic code interoperate without breaking millions of");
        System.out.println("  existing class files. C# added generics later WITH a breaking");
        System.out.println("  runtime change; Java chose not to. A defensible trade - and it");
        System.out.println("  costs you everything below.");

        System.out.println();
        System.out.println("  WHAT ERASURE FORBIDS:");
        System.out.printf("    %-40s %s%n", "new T()", "cannot instantiate a type parameter");
        System.out.printf("    %-40s %s%n", "new T[10]", "cannot create a generic array");
        System.out.printf("    %-40s %s%n", "x instanceof List<String>", "cannot test a parameterised type");
        System.out.printf("    %-40s %s%n", "class MyEx<T> extends Exception", "generic Throwables are banned");
        System.out.printf("    %-40s %s%n", "static T field;", "no static member of the class's T");
        System.out.printf("    %-40s %s%n", "f(List<String>) and f(List<Integer>)", "SAME erasure - cannot coexist");

        System.out.println();
        System.out.println("  That last one is worth remembering:");
        System.out.println("      void f(List<String> l) { }");
        System.out.println("      void f(List<Integer> l) { }");
        System.out.println("      -> error: name clash: both methods have the same erasure");

        System.out.println();
        System.out.println("  instanceof CAN test the raw type, and that is all:");
        Object something = new ArrayList<String>();
        System.out.println("    something instanceof List    -> " + (something instanceof List));
        System.out.println("    something instanceof List<?> -> " + (something instanceof List<?>)
                + "   (legal, and tells you nothing more)");
        System.out.println("    something instanceof List<String> -> COMPILE ERROR");

        System.out.println();
        System.out.println("  WORKING AROUND new T() - pass a factory:");
        System.out.println("      static <T> T create(Supplier<T> factory)");
        List<String> created = create(ArrayList::new);
        created.add("built via a Supplier");
        System.out.println("    create(ArrayList::new) -> " + created);
        System.out.println("    Or pass a Class token: create(String.class) and use reflection.");


        /* ====================================================================
         * SECTION 4 - INVARIANCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - List<String> IS NOT A List<Object>");
        System.out.println("=".repeat(74));

        System.out.println("      List<String> strings = new ArrayList<>();");
        System.out.println("      List<Object> objects = strings;   // COMPILE ERROR");
        System.out.println();
        System.out.println("  WHY - because it would break, exactly like this:");
        System.out.println("      List<Object> objects = strings;   // if this were allowed...");
        System.out.println("      objects.add(42);                  // ...put an Integer in...");
        System.out.println("      String s = strings.get(0);        // ...ClassCastException");

        System.out.println();
        System.out.println("  ARRAYS ARE COVARIANT AND HAVE EXACTLY THAT HOLE (lesson 12):");
        Object[] covariantArray = new String[1];
        System.out.println("    Object[] o = new String[1];   -> compiles fine");
        try {
            covariantArray[0] = 42;
        } catch (ArrayStoreException e) {
            System.out.println("    o[0] = 42;                    -> ArrayStoreException at RUNTIME");
            System.out.println("      " + e.getMessage());
        }
        System.out.println();
        System.out.println("  Generics moved that error to COMPILE time deliberately. The");
        System.out.println("  array version fails when it runs; the generic version never");
        System.out.println("  compiles. That is the whole argument for invariance.");


        /* ====================================================================
         * SECTION 5 - WILDCARDS AND PECS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - PECS: PRODUCER EXTENDS, CONSUMER SUPER");
        System.out.println("=".repeat(74));

        System.out.println("  Invariance is safe but restrictive. This does not compile:");
        System.out.println("      double sumStrict(List<Number> numbers)");
        System.out.println("      sumStrict(List.of(1, 2, 3));   // a List<Integer>: REJECTED");
        System.out.println();
        System.out.println("  ? extends T - a PRODUCER, which you READ from:");
        System.out.println("      double sum(List<? extends Number> numbers)");
        System.out.println("    sum(List<Integer>) -> " + sum(List.of(1, 2, 3)) + "   accepted");
        System.out.println("    sum(List<Double>)  -> " + sum(List.of(1.5, 2.5)) + "   accepted");
        System.out.println("    sum(List<Long>)    -> " + sum(List.of(10L, 20L)) + "  accepted");
        System.out.println();
        System.out.println("    You can READ as Number, but CANNOT add:");
        System.out.println("        numbers.add(1);   -> COMPILE ERROR");
        System.out.println("    because the compiler does not know whether the real list is");
        System.out.println("    a List<Integer> or a List<Double>.");

        System.out.println();
        System.out.println("  ? super T - a CONSUMER, which you WRITE to:");
        List<Integer> integerTarget = new ArrayList<>();
        List<Number> numberTarget = new ArrayList<>();
        List<Object> objectTarget = new ArrayList<>();

        addNumbers(integerTarget);
        addNumbers(numberTarget);
        addNumbers(objectTarget);

        System.out.println("      void addNumbers(List<? super Integer> target)");
        System.out.println("    addNumbers(new ArrayList<Integer>()) -> " + integerTarget);
        System.out.println("    addNumbers(new ArrayList<Number>())  -> " + numberTarget);
        System.out.println("    addNumbers(new ArrayList<Object>())  -> " + objectTarget);
        System.out.println();
        System.out.println("    You can WRITE Integers, but reading gives only Object:");
        System.out.println("        Integer x = target.get(0);   -> COMPILE ERROR");

        System.out.println();
        System.out.println("  THE RULE:");
        System.out.println("    the parameter PRODUCES values you read  -> ? extends T");
        System.out.println("    the parameter CONSUMES values you write -> ? super T");
        System.out.println("    it does BOTH                            -> plain T");

        System.out.println();
        System.out.println("  Both at once - the JDK's own copy signature:");
        List<Object> destination = new ArrayList<>(List.of("existing"));
        copy(destination, List.of(1, 2, 3));
        System.out.println("      static <T> void copy(List<? super T> dest, List<? extends T> src)");
        System.out.println("    after copy -> " + destination);

        System.out.println();
        System.out.println("  THE JDK FOLLOWS PECS EXACTLY:");
        System.out.println("      Stream<T> filter(Predicate<? super T> predicate)");
        System.out.println("      <R> Stream<R> map(Function<? super T, ? extends R> mapper)");
        System.out.println("      boolean addAll(Collection<? extends E> c)");
        System.out.println("    Read those signatures again now - they should make sense.");

        System.out.println();
        System.out.println("  THE UNBOUNDED WILDCARD - List<?> means 'a list of something':");
        System.out.println("    sizeOf(List.of(1, 2, 3))     -> " + sizeOf(List.of(1, 2, 3)));
        System.out.println("    sizeOf(List.of(\"a\", \"b\"))    -> " + sizeOf(List.of("a", "b")));
        System.out.println();
        System.out.println("    List<?> is NOT List<Object>: you can pass a List<String> to");
        System.out.println("    the first and not the second. You may not add anything to a");
        System.out.println("    List<?> except null.");

        System.out.println();
        System.out.println("  WHERE TO PUT WILDCARDS: on PARAMETERS, not return types.");
        System.out.println("      List<? extends Number> getNumbers()   // forces wildcards");
        System.out.println("                                            // on every caller");
        System.out.println("      List<Number> getNumbers()             // better");


        /* ====================================================================
         * SECTION 6 - UNCHECKED WARNINGS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - WHAT AN UNCHECKED WARNING MEANS");
        System.out.println("=".repeat(74));

        System.out.println("      List<String> list = (List<String>) someRawList;");
        System.out.println("      -> warning: [unchecked] unchecked cast");
        System.out.println();
        System.out.println("  The compiler is telling you it CANNOT VERIFY the cast, because");
        System.out.println("  the type information was erased. The runtime check you might");
        System.out.println("  expect DOES NOT HAPPEN:");

        List<String> deceptive = uncheckedCast(rawList);
        System.out.println();
        System.out.println("    the cast itself succeeded, silently -> " + deceptive);
        System.out.println("    even though element 1 is an Integer.");
        try {
            String value = deceptive.get(1);
            System.out.println("    unreachable: " + value);
        } catch (ClassCastException e) {
            System.out.println("    deceptive.get(1) -> ClassCastException, but only NOW -");
            System.out.println("    at the point of USE, far from the cast that caused it.");
        }

        System.out.println();
        System.out.println("  Only suppress when you have PROVED the cast is safe, on the");
        System.out.println("  NARROWEST scope, with a comment saying why:");
        System.out.println();
        System.out.println("      @SuppressWarnings(\"unchecked\")   // safe: only Strings go in");
        System.out.println("      List<String> result = (List<String>) raw;");
        System.out.println();
        System.out.println("  Compile with -Xlint:unchecked to see every one.");
        System.out.println();
        System.out.println("  HEAP POLLUTION (lesson 19) is the same idea for varargs:");
        System.out.println("  a generic varargs array can escape and be typed wrongly.");
        System.out.println("  Annotate genuinely safe methods with @SafeVarargs.");


        /* ====================================================================
         * SECTION 7 - RAW TYPES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - NEVER USE RAW TYPES");
        System.out.println("=".repeat(74));

        System.out.println("  A raw type turns off generic checking for the WHOLE variable -");
        System.out.println("  including for the parts you thought were still safe:");
        System.out.println();

        @SuppressWarnings("rawtypes")
        List rawAgain = new ArrayList<String>();      // declared with <String>!
        rawAgain.add("fine");
        rawAgain.add(42);                              // accepted anyway

        System.out.println("    List raw = new ArrayList<String>();   // note the <String>");
        System.out.println("    raw.add(42);                          -> STILL ACCEPTED");
        System.out.println("    contents: " + rawAgain);
        System.out.println();
        System.out.println("    Assigning to a RAW variable discarded the checking entirely.");

        System.out.println();
        System.out.println("  Raw types exist ONLY for pre-Java-5 compatibility.");
        System.out.println("  Use List<?> when you genuinely do not care about the element type:");
        System.out.println("    sizeOf(rawAgain) -> " + sizeOf(rawAgain)
                + "   (safe: List<?> only lets you read Object)");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 43.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 2 - GENERIC METHODS AND BOUNDS
    // ------------------------------------------------------------------------

    /**
     * A generic method: the type parameter goes BEFORE the return type.
     *
     * @param <T>    the source element type
     * @param <R>    the result element type
     * @param source the input list
     * @param mapper how to convert each element
     * @return a new list of converted elements
     */
    static <T, R> List<R> map(List<T> source, Function<T, R> mapper) {
        List<R> result = new ArrayList<>();
        for (T item : source) {
            result.add(mapper.apply(item));
        }
        return result;
    }

    /**
     * BOUNDED, and using a wildcard so any list of any Number subtype is
     * accepted. The bound is what permits the doubleValue() call.
     *
     * @param numbers any list producing Numbers
     * @return their total
     */
    static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number number : numbers) {
            total += number.doubleValue();      // allowed - the bound guarantees it
        }
        return total;
    }

    /**
     * MULTIPLE BOUNDS with &. T must be both Comparable and Serializable.
     *
     * @param <T>   a type that is both comparable and serializable
     * @param items the items to search; must not be empty
     * @return the largest
     */
    static <T extends Comparable<T> & Serializable> T largest(List<T> items) {
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) > 0) {
                best = item;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------------
    // SECTION 3 - WORKING AROUND ERASURE
    // ------------------------------------------------------------------------

    /**
     * You cannot write `new T()`, so take a factory instead.
     *
     * @param <T>     the type to create
     * @param factory how to create one
     * @return a new instance
     */
    static <T> T create(Supplier<T> factory) {
        return factory.get();
    }

    // ------------------------------------------------------------------------
    // SECTION 5 - PECS
    // ------------------------------------------------------------------------

    /**
     * A CONSUMER: we write Integers into it, so `? super Integer`. Any list
     * that can hold an Integer is acceptable - Integer, Number or Object.
     *
     * @param target the list to add to
     */
    static void addNumbers(List<? super Integer> target) {
        target.add(1);
        target.add(2);
        // Integer first = target.get(0);
        //   ERROR: only Object is guaranteed on the way out.
    }

    /**
     * BOTH at once - the shape the JDK's Collections.copy uses.
     *
     * @param <T>         the element type
     * @param destination consumes elements, so ? super T
     * @param source      produces elements, so ? extends T
     */
    static <T> void copy(List<? super T> destination, List<? extends T> source) {
        for (T item : source) {
            destination.add(item);
        }
    }

    /**
     * The unbounded wildcard: any list at all, and we only read Object-level
     * information from it.
     *
     * @param list any list
     * @return its size
     */
    static int sizeOf(List<?> list) {
        // list.add("anything");
        //   ERROR: you may add nothing but null to a List<?>
        return list.size();
    }

    // ------------------------------------------------------------------------
    // SECTION 6 - UNCHECKED CASTS
    // ------------------------------------------------------------------------

    /**
     * Performs an unchecked cast, which succeeds silently because the type
     * information was erased. The failure surfaces later, at the point of use.
     *
     * @param raw a raw list that may contain anything
     * @return the same list, claimed to be a List of Strings
     */
    @SuppressWarnings("unchecked")
    static List<String> uncheckedCast(@SuppressWarnings("rawtypes") List raw) {
        // NOT safe - this is a demonstration of what the warning means. Real
        // suppression requires a proof and a comment (see the lesson).
        return (List<String>) raw;
    }
}

/**
 * A generic class. T is a TYPE PARAMETER, erased to Object at runtime.
 *
 * @param <T> what this box holds
 */
class Box<T> {

    private T content;

    // static T shared;
    //   ERROR: non-static type variable T cannot be referenced from a static
    //   context. There is one Box class for every T, so there is no single
    //   type a static field could have.

    /** @param content what to put in */
    void put(T content) {
        this.content = content;
    }

    /** @return what is in the box, already typed - no cast needed */
    T get() {
        return content;
    }

    // T createNew() { return new T(); }
    //   ERROR: type parameter T cannot be instantiated directly. T is Object
    //   at runtime, so there is no constructor to call.

    // T[] toArray() { return new T[10]; }
    //   ERROR: generic array creation. Same reason.
}

// class GenericException<T> extends Exception { }
//   ERROR: a generic class may not extend Throwable. Catch clauses are matched
//   at runtime, and erasure would make `catch (GenericException<String> e)`
//   indistinguishable from `catch (GenericException<Integer> e)`.

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Uncomment each commented-out error in the Box class and read the exact
 *    compiler message. Then explain each one using the word "erasure".
 *
 * 2. Write `static <T> void addAll(List<? super T> dest, T... items)`. Then
 *    call it with a List<Object> and some Strings. Note the @SafeVarargs
 *    question from lesson 19.
 *
 * 3. Change sum's parameter from List<? extends Number> to List<Number> and
 *    find every call site that stops compiling. That is what PECS buys.
 *
 * 4. Write a Pair<K, V> class with a `swap()` returning Pair<V, K>. Then add
 *    a static factory `of(K, V)` and note where you needed <K, V> and where
 *    inference handled it.
 *
 * 5. Compile this file with -Xlint:unchecked and read every warning. Then
 *    narrow each @SuppressWarnings to the smallest possible scope.
 *
 * 6. Try to write two overloads f(List<String>) and f(List<Integer>). Read the
 *    error, then solve the problem a different way - with one method and a
 *    Class token, or with two differently-named methods.
 * ============================================================================
 */
