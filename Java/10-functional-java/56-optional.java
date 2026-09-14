/* ============================================================================
 * 56 - Optional
 * ----------------------------------------------------------------------------
 * Companion lesson: 56-optional.md
 *
 * RUN IT:
 *     java Java/10-functional-java/56-optional.java
 *
 * Section 4 catches the single most common Optional performance mistake:
 * orElse's argument is ALWAYS evaluated, even when the Optional is present.
 * Section 6 shows why Optional as a FIELD defeats its own purpose.
 * ============================================================================
 */

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

class OptionalLesson {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - CREATING AN Optional, THREE WAYS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - Optional.of, Optional.ofNullable, Optional.empty");
        System.out.println("=".repeat(74));

        Optional<String> present = Optional.of("hello");
        Optional<String> empty = Optional.empty();
        System.out.println("    Optional.of(\"hello\")  -> " + present);
        System.out.println("    Optional.empty()       -> " + empty);

        System.out.println();
        System.out.println("    THE REAL DIFFERENCE - Optional.of REJECTS null IMMEDIATELY:");
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("      Optional.of(null)        -> NullPointerException, RIGHT HERE,");
            System.out.println("      not silently wrapped and deferred to later - fail FAST.");
        }
        Optional<String> safelyEmpty = Optional.ofNullable(null);
        System.out.println("      Optional.ofNullable(null) -> " + safelyEmpty + "   (safe - use this");
        System.out.println("      whenever the value genuinely might be null, e.g. from a Map.get)");

        Map<String, String> config = Map.of("host", "localhost");
        String missingValue = config.get("port");   // returns null - key not present
        System.out.println();
        System.out.println("    map.get(\"port\") on a missing key -> " + missingValue);
        System.out.println("    Optional.of(missingValue) would THROW - wrap Map lookups with");
        System.out.println("    Optional.ofNullable(map.get(key)), always.");


        /* ====================================================================
         * SECTION 2 - THE ANTI-PATTERN: get() WITHOUT CHECKING FIRST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - .get() ON AN EMPTY Optional: A REAL EXCEPTION");
        System.out.println("=".repeat(74));

        System.out.println("    .isPresent() / .isEmpty() -> " + present.isPresent() + " / " + present.isEmpty());
        try {
            empty.get();
        } catch (NoSuchElementException e) {
            System.out.println("    empty.get() -> NoSuchElementException: \"" + e.getMessage() + "\"");
        }
        System.out.println();
        System.out.println("    if (opt.isPresent()) { opt.get() } WORKS, but it is exactly the");
        System.out.println("    if-null-check pattern Optional exists to get code AWAY from. The");
        System.out.println("    idioms in Sections 3-5 are the actual point of this type.");


        /* ====================================================================
         * SECTION 3 - orElse, orElseThrow, ifPresent
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - orElse, orElseThrow, ifPresent, ifPresentOrElse");
        System.out.println("=".repeat(74));

        System.out.println("    present.orElse(\"default\")            -> " + present.orElse("default"));
        System.out.println("    empty.orElse(\"default\")              -> " + empty.orElse("default"));

        try {
            empty.orElseThrow(() -> new IllegalStateException("config value required"));
        } catch (IllegalStateException e) {
            System.out.println("    empty.orElseThrow(customException)  -> IllegalStateException: \""
                    + e.getMessage() + "\"");
        }

        present.ifPresent(v -> System.out.println("    present.ifPresent(...)               -> ran, value = " + v));
        empty.ifPresent(v -> System.out.println("    THIS LINE NEVER PRINTS - empty is empty"));

        empty.ifPresentOrElse(
                v -> System.out.println("    present branch"),
                () -> System.out.println("    empty.ifPresentOrElse(...)           -> ran the EMPTY branch"));


        /* ====================================================================
         * SECTION 4 - orElse vs orElseGet: THE REAL, MEASURABLE DIFFERENCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - orElse's ARGUMENT IS ALWAYS EVALUATED. ALWAYS.");
        System.out.println("=".repeat(74));

        System.out.println("    computeExpensiveDefault() below just PRINTS when it runs, so we");
        System.out.println("    can see EXACTLY when it is called:");

        System.out.println();
        System.out.println("    present.orElse(computeExpensiveDefault()) - present IS present,");
        System.out.println("    the default should be UNNECESSARY:");
        String r1 = present.orElse(computeExpensiveDefault());
        System.out.println("      result -> " + r1);

        System.out.println();
        System.out.println("    present.orElseGet(() -> computeExpensiveDefault()) - SAME situation:");
        String r2 = present.orElseGet(OptionalLesson::computeExpensiveDefault);
        System.out.println("      result -> " + r2);

        System.out.println();
        System.out.println("    THE DIFFERENCE: orElse's argument is an already-evaluated VALUE -");
        System.out.println("    Java evaluates method arguments BEFORE the call, unconditionally,");
        System.out.println("    the same as any other method call. orElseGet takes a SUPPLIER,");
        System.out.println("    which is only INVOKED if actually needed - genuinely lazy. For a");
        System.out.println("    cheap literal default this difference is invisible. For an");
        System.out.println("    EXPENSIVE default (a database call, a network request, a heavy");
        System.out.println("    computation) orElse pays that cost EVERY TIME, even when the");
        System.out.println("    Optional was already present and the default was never needed.");


        /* ====================================================================
         * SECTION 5 - map, flatMap, filter: CHAINING WITHOUT isPresent CHECKS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - map/flatMap/filter: FUNCTIONAL CHAINING");
        System.out.println("=".repeat(74));

        Optional<String> name = Optional.of("alice");
        Optional<Integer> nameLength = name.map(String::length);
        System.out.println("    Optional.of(\"alice\").map(String::length) -> " + nameLength);

        Optional<String> upperIfLong = name
                .filter(n -> n.length() > 3)
                .map(String::toUpperCase);
        System.out.println("    .filter(len > 3).map(toUpperCase)         -> " + upperIfLong);

        Optional<String> shortName = Optional.of("al");
        Optional<String> upperIfLongShort = shortName
                .filter(n -> n.length() > 3)
                .map(String::toUpperCase);
        System.out.println("    same chain on \"al\" (fails the filter)     -> " + upperIfLongShort);

        System.out.println();
        System.out.println("    map() on an EMPTY Optional short-circuits SAFELY - no null check");
        System.out.println("    needed anywhere in the chain:");
        Optional<Integer> emptyMapped = Optional.<String>empty().map(String::length);
        System.out.println("      Optional.<String>empty().map(String::length) -> " + emptyMapped);

        Address address = new Address("Springfield");
        Person personWithAddress = new Person(Optional.of(address));
        Person personWithoutAddress = new Person(Optional.empty());

        System.out.println();
        System.out.println("    flatMap - CHAINING through a NESTED Optional field, no nested if:");
        Optional<String> city1 = personWithAddress.address().flatMap(Address::cityOptional);
        Optional<String> city2 = personWithoutAddress.address().flatMap(Address::cityOptional);
        System.out.println("      person WITH address    -> " + city1);
        System.out.println("      person WITHOUT address -> " + city2);


        /* ====================================================================
         * SECTION 6 - Optional IS FOR RETURN TYPES. NOT FIELDS. NOT PARAMETERS.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - Optional AS A FIELD DEFEATS ITS OWN PURPOSE");
        System.out.println("=".repeat(74));

        System.out.println("    Optional's ENTIRE point is: a method's RETURN TYPE tells callers");
        System.out.println("    \"this might not have a value\" without them needing to remember to");
        System.out.println("    null-check. That guarantee lives in the TYPE SIGNATURE.");
        System.out.println();
        System.out.println("    Using Optional<T> as a FIELD does not extend that guarantee - it");
        System.out.println("    just moves the SAME null-check problem one level, because the");
        System.out.println("    Optional REFERENCE ITSELF can still be null:");

        BrokenPerson brokenPerson = new BrokenPerson();   // address field never set - still null
        System.out.println();
        System.out.println("      BrokenPerson (address field NEVER assigned):");
        try {
            brokenPerson.address.isPresent();
        } catch (NullPointerException e) {
            System.out.println("        brokenPerson.address.isPresent() -> NullPointerException");
            System.out.println("        the Optional<Address> FIELD ITSELF was null - Optional");
            System.out.println("        promised nothing here, because nothing FORCED it to be set.");
        }
        System.out.println();
        System.out.println("    Optional is also NOT Serializable and carries real allocation cost");
        System.out.println("    per instance - the JDK's own design intent (Brian Goetz, its");
        System.out.println("    architect, has stated this directly) is RETURN TYPES ONLY. For a");
        System.out.println("    field that might be absent, use null with real discipline, or a");
        System.out.println("    proper default value - not Optional.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 56, and of Section 10.");
        System.out.println("=".repeat(74));
    }

    static String computeExpensiveDefault() {
        System.out.println("      *** computeExpensiveDefault() ACTUALLY RAN ***");
        return "default";
    }
}

record Address(String city) {
    // a SEPARATE method (cannot reuse the canonical accessor's name/signature) that
    // wraps the field for the flatMap demonstration below
    Optional<String> cityOptional() {
        return Optional.ofNullable(city);
    }
}

record Person(Optional<Address> address) {
}

/** Deliberately broken - an Optional FIELD that is never initialized. */
class BrokenPerson {
    Optional<Address> address;   // defaults to null, like any other reference field
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write a method findUserById(int id) returning Optional<String> that is
 *    empty for unknown ids - call it with .orElseThrow() and a custom
 *    exception, and again with .orElse("Unknown").
 *
 * 2. Reproduce Section 4's experiment with orElseGet SKIPPING the call -
 *    make BOTH branches present and confirm computeExpensiveDefault()
 *    prints for orElse but NOT for orElseGet.
 *
 * 3. Chain three .map() calls on an Optional<String> (trim, then
 *    toLowerCase, then length) and run it once with a present value and
 *    once with Optional.empty() - confirm neither branch throws.
 *
 * 4. Build a THREE-level nested Optional chain (Person -> Address ->
 *    PostalCode, all Optional-wrapped) using flatMap, and test all four
 *    combinations of present/absent at each level.
 *
 * 5. Explain, in your own words, why Optional<List<String>> as a return
 *    type is usually WORSE than just returning an EMPTY List<String> -
 *    what does Optional add here that an empty collection does not
 *    already communicate?
 * ============================================================================
 */
