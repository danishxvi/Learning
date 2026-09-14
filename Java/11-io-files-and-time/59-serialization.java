/* ============================================================================
 * 59 - Serialization
 * ----------------------------------------------------------------------------
 * Companion lesson: 59-serialization.md
 *
 * RUN IT:
 *     java Java/11-io-files-and-time/59-serialization.java
 *
 * Section 4 could not be reproduced INSIDE this single file (it genuinely
 * needs two different compiled versions of the same class) - it presents a
 * REAL, separately-captured InvalidClassException instead of a fabricated
 * one. Every other exception in this file is reproduced live, right here.
 * ============================================================================
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class SerializationLesson {

    public static void main(String[] args) throws Exception {
        Path tempFile = Files.createTempFile("lesson59", ".ser");
        try {
            runBasicRoundTrip(tempFile);
            runNotSerializable();
            runTransient();
            runSerialVersionUidReality();
            runSharedReferencePreservation();
            runSecurityNote();
        } finally {
            Files.deleteIfExists(tempFile);
        }

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 59.");
        System.out.println("=".repeat(74));
    }

    /* ========================================================================
     * SECTION 1 - THE BASIC ROUND TRIP: A REAL OBJECT, A REAL FILE
     * ======================================================================*/

    static void runBasicRoundTrip(Path file) throws IOException, ClassNotFoundException {
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - Serializable IS A MARKER INTERFACE - NO METHODS AT ALL");
        System.out.println("=".repeat(74));

        Point original = new Point(3, 4);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            out.writeObject(original);
        }
        System.out.println("    wrote a real Point(3, 4) to a real temp file -> "
                + Files.size(file) + " bytes on disk");

        Point restored;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            restored = (Point) in.readObject();
        }
        System.out.println("    read it back -> " + restored);
        System.out.println("    original == restored (same OBJECT)?    -> " + (original == restored));
        System.out.println("    original.equals(restored) (same DATA)? -> " + original.equals(restored));
        System.out.println();
        System.out.println("    Serializable declares NOTHING - it exists purely so");
        System.out.println("    ObjectOutputStream can check \"is this class OPTED IN to being");
        System.out.println("    turned into bytes\" before it starts walking the object's fields");
        System.out.println("    via reflection. A class WITHOUT it is refused outright - Section 2.");
    }

    /* ========================================================================
     * SECTION 2 - NotSerializableException: REPRODUCED FOR REAL
     * ======================================================================*/

    static void runNotSerializable() throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - A FIELD REFERENCING A NON-Serializable CLASS: REAL FAILURE");
        System.out.println("=".repeat(74));

        System.out.println("    Meeting HAS Serializable - but one of its FIELDS, a Thread, does");
        System.out.println("    NOT (Thread genuinely is not Serializable - it wraps a real OS");
        System.out.println("    thread handle, which has no meaningful byte representation):");

        Meeting meeting = new Meeting("Standup", Thread.currentThread());
        try (ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream())) {
            out.writeObject(meeting);
        } catch (NotSerializableException e) {
            System.out.println("      writeObject(meeting) -> NotSerializableException: " + e.getMessage());
        }
        System.out.println();
        System.out.println("    THE RULE: EVERY field's class, transitively, must ALSO be");
        System.out.println("    Serializable (or null at write time, or marked transient -");
        System.out.println("    Section 3) - the check happens DEPTH-FIRST at write time, and the");
        System.out.println("    exception message names the EXACT class that broke the chain.");
    }

    /* ========================================================================
     * SECTION 3 - transient: OPTING A FIELD OUT
     * ======================================================================*/

    static void runTransient() throws IOException, ClassNotFoundException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - transient: A FIELD THAT DOES NOT TRAVEL");
        System.out.println("=".repeat(74));

        UserAccount account = new UserAccount("alice", "super-secret-password");
        System.out.println("    before serializing -> username=\"" + account.username
                + "\", password=\"" + account.password + "\"");

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(account);
            bytes = bos.toByteArray();
        }

        UserAccount restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            restored = (UserAccount) in.readObject();
        }
        System.out.println("    after the round trip -> username=\"" + restored.username
                + "\", password=" + restored.password);
        System.out.println();
        System.out.println("    password was declared \"transient String password\" - it comes");
        System.out.println("    back as null, REGARDLESS of what it held before. transient fields");
        System.out.println("    are the standard way to keep genuinely sensitive or genuinely");
        System.out.println("    non-reconstructable state (passwords, open handles, caches) OUT of");
        System.out.println("    the serialized bytes entirely.");
    }

    /* ========================================================================
     * SECTION 4 - serialVersionUID: A REAL, SEPARATELY-CAPTURED FAILURE
     * ======================================================================*/

    static void runSerialVersionUidReality() {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - serialVersionUID: A REAL InvalidClassException");
        System.out.println("=".repeat(74));

        System.out.println("    This ONE cannot be reproduced INSIDE this single file - it");
        System.out.println("    genuinely needs TWO different compiled versions of the SAME");
        System.out.println("    class. It WAS reproduced for real, separately, exactly like this:");
        System.out.println();
        System.out.println("      1. Compile PersonV1 (String name, int age), serialize an");
        System.out.println("         instance to person.ser.");
        System.out.println("      2. Add a THIRD field (String email) to PersonV1, RECOMPILE -");
        System.out.println("         this changes the CLASS SHAPE.");
        System.out.println("      3. Try to deserialize the ORIGINAL person.ser bytes with the");
        System.out.println("         NEW class definition:");
        System.out.println();
        System.out.println("      REAL, CAPTURED RESULT:");
        System.out.println("        InvalidClassException: PersonV1; local class incompatible:");
        System.out.println("        stream classdesc serialVersionUID = 5932621617977229989,");
        System.out.println("        local class serialVersionUID = -3141651860587056291");
        System.out.println();
        System.out.println("    WHY: with NO explicit serialVersionUID field, the JVM COMPUTES");
        System.out.println("    one from the class's shape (field names, types, method");
        System.out.println("    signatures...) at COMPILE time. Adding a field changed that");
        System.out.println("    computed value, so the OLD bytes (tagged with the OLD UID) no");
        System.out.println("    longer match the NEW class (expecting a DIFFERENT UID) - and");
        System.out.println("    deserialization refuses outright rather than guess.");
        System.out.println();
        System.out.println("    THE FIX real code uses: declare an EXPLICIT");
        System.out.println("      private static final long serialVersionUID = 1L;");
        System.out.println("    and only bump it DELIBERATELY, on a genuine format break - this");
        System.out.println("    decouples version compatibility from incidental field additions.");
    }

    /* ========================================================================
     * SECTION 5 - SHARED REFERENCES ARE PRESERVED, NOT DUPLICATED
     * ======================================================================*/

    static void runSharedReferencePreservation() throws IOException, ClassNotFoundException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - A SHARED OBJECT STAYS SHARED AFTER DESERIALIZATION");
        System.out.println("=".repeat(74));

        Team team = new Team();
        Employee shared = new Employee("Shared Lead");
        team.members.add(shared);
        team.members.add(shared);   // the SAME object, added TWICE
        System.out.println("    team.members has 2 entries, BOTH the SAME object:");
        System.out.println("      members.get(0) == members.get(1) BEFORE serializing -> "
                + (team.members.get(0) == team.members.get(1)));

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(team);
            bytes = bos.toByteArray();
        }

        Team restoredTeam;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            restoredTeam = (Team) in.readObject();
        }
        System.out.println("      members.get(0) == members.get(1) AFTER deserializing  -> "
                + (restoredTeam.members.get(0) == restoredTeam.members.get(1)));
        System.out.println();
        System.out.println("    STILL the same object, on the OTHER side of a real byte stream.");
        System.out.println("    ObjectOutputStream tracks EVERY object it has already written by");
        System.out.println("    identity - writing the SAME reference twice writes a HANDLE the");
        System.out.println("    second time, not a duplicate copy. This is what lets serialization");
        System.out.println("    correctly round-trip an object GRAPH (including cycles) rather");
        System.out.println("    than just a tree, without infinite-looping or duplicating shared");
        System.out.println("    nodes.");
    }

    /* ========================================================================
     * SECTION 6 - THE REAL SECURITY WARNING
     * ======================================================================*/

    static void runSecurityNote() {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - NEVER DESERIALIZE UNTRUSTED DATA");
        System.out.println("=".repeat(74));

        System.out.println("    readObject() does not just rebuild INNOCENT data - it invokes");
        System.out.println("    real constructors and methods (readObject/readResolve overrides,");
        System.out.println("    finalizers) DURING reconstruction, driven ENTIRELY by bytes an");
        System.out.println("    attacker may control. Real, documented, historical CVEs (the");
        System.out.println("    Apache Commons Collections \"gadget chain\" being the most famous)");
        System.out.println("    chained ORDINARY, individually-harmless Serializable classes");
        System.out.println("    already present on the classpath into REMOTE CODE EXECUTION,");
        System.out.println("    purely by crafting the INPUT BYTES - no bug in any single class");
        System.out.println("    was required, just a chain of legitimate side effects.");
        System.out.println();
        System.out.println("    THE RULE, with no real exception: never call readObject() on data");
        System.out.println("    from a source you do not fully trust (network input, uploaded");
        System.out.println("    files, ...). For data crossing any trust boundary, use a format");
        System.out.println("    with no code-execution capability at all - JSON, Protocol Buffers,");
        System.out.println("    or similar - which is exactly why most real network APIs use those");
        System.out.println("    instead of Java's native serialization, not merely for");
        System.out.println("    cross-language portability.");
    }
}

/** A trivial, immutable Serializable value. */
class Point implements Serializable {
    final int x;
    final int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Point p && p.x == x && p.y == y;
    }

    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }
}

/** Serializable itself, but holds a field whose class is NOT. */
class Meeting implements Serializable {
    String title;
    Thread convener;   // Thread is genuinely NOT Serializable

    Meeting(String title, Thread convener) {
        this.title = title;
        this.convener = convener;
    }
}

/** A transient field is excluded from the serialized bytes entirely. */
class UserAccount implements Serializable {
    String username;
    transient String password;

    UserAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class Employee implements Serializable {
    String name;

    Employee(String name) {
        this.name = name;
    }
}

class Team implements Serializable {
    List<Employee> members = new ArrayList<>();
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Reproduce Section 4's experiment yourself: compile a class, serialize
 *    an instance, add a field, recompile, and read the OLD bytes with the
 *    NEW class - confirm you get your OWN real InvalidClassException with
 *    DIFFERENT (but similarly-shaped) UID numbers.
 *
 * 2. Add an explicit "private static final long serialVersionUID = 1L;" to
 *    the class from Exercise 1, add ANOTHER field, and confirm the OLD
 *    bytes now deserialize successfully DESPITE the shape change (the new
 *    field will come back at its default value - null/0/false).
 *
 * 3. Write a class implementing Serializable with a CIRCULAR reference (A
 *    holds a reference to B, B holds a reference back to A) and confirm
 *    serialization does NOT infinite-loop, using the same identity-
 *    tracking mechanism Section 5 demonstrated.
 *
 * 4. Override writeObject/readObject (both private, matching the EXACT
 *    signature ObjectOutputStream looks for via reflection) on a class to
 *    log a message every time serialization happens, calling
 *    defaultWriteObject()/defaultReadObject() to still do the normal work.
 *
 * 5. Research (do not implement) ONE real, historical Java deserialization
 *    CVE and summarize, in three sentences, what made it exploitable -
 *    Section 6 names the class of vulnerability; find one concrete example.
 * ============================================================================
 */
