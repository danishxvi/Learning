/* ============================================================================
 * 58 - NIO.2: Path, Files and directory walking
 * ----------------------------------------------------------------------------
 * Companion lesson: 58-nio-files-and-paths.md
 *
 * RUN IT:
 *     java Java/11-io-files-and-time/58-nio-files-and-paths.java
 *
 * This lesson builds and tears down a REAL small directory tree on disk.
 * Section 5 reproduces a REAL OS-level permission error - Windows genuinely
 * refuses to create a symbolic link without elevated privileges.
 * ============================================================================
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

class NioFilesAndPaths {

    public static void main(String[] args) throws IOException {
        Path root = Files.createTempDirectory("lesson58");
        try {
            runPathBasics(root);
            runFilesUtility(root);
            runDirectoryWalking(root);
            runFileAttributes(root);
            runSymlinkPermissionReality(root);
        } finally {
            deleteRecursively(root);
            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("  End of lesson 58. The entire temp tree was deleted: "
                    + !Files.exists(root));
            System.out.println("=".repeat(74));
        }
    }

    /* ========================================================================
     * SECTION 1 - Path: THE MODERN REPLACEMENT FOR java.io.File
     * ======================================================================*/

    static void runPathBasics(Path root) {
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - Path: COMPOSITION, NOT STRING CONCATENATION");
        System.out.println("=".repeat(74));

        System.out.println("    root (a REAL temp directory)  -> " + root);

        Path child = root.resolve("data").resolve("2024").resolve("report.txt");
        System.out.println("    root.resolve(\"data\").resolve(\"2024\").resolve(\"report.txt\")");
        System.out.println("      -> " + child);
        System.out.println("    getFileName()  -> " + child.getFileName());
        System.out.println("    getParent()    -> " + child.getParent());
        System.out.println("    getNameCount() -> " + child.getNameCount() + "   (path SEGMENTS, from the root)");

        Path messy = root.resolve("a").resolve("..").resolve("b").resolve(".").resolve("c");
        System.out.println();
        System.out.println("    a MESSY path with .. and . segments -> " + messy);
        System.out.println("    .normalize()                         -> " + messy.normalize()
                + "   (resolves .. and . WITHOUT touching the file system)");

        Path absolute = child.toAbsolutePath();
        Path relative = absolute.getParent().relativize(child);
        System.out.println();
        System.out.println("    toAbsolutePath()                     -> " + absolute);
        System.out.println("    parent.relativize(child)             -> " + relative);
        System.out.println();
        System.out.println("    NONE of this touched the file system - Path is PURE STRING/");
        System.out.println("    SEGMENT manipulation. java.io.File mixed path logic and actual");
        System.out.println("    I/O in one class; NIO.2 splits them - Path for the STRUCTURE,");
        System.out.println("    Files for the OPERATIONS, as Section 2 shows.");
    }

    /* ========================================================================
     * SECTION 2 - Files: THE OPERATIONS, AND HOW SHORT THEY ARE NOW
     * ======================================================================*/

    static void runFilesUtility(Path root) throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - Files: ONE-LINE OPERATIONS, LESSON 57's STACK IN ONE CALL");
        System.out.println("=".repeat(74));

        Path file = root.resolve("hello.txt");
        Files.writeString(file, "Hello, NIO.2!", StandardCharsets.UTF_8);
        System.out.println("    Files.writeString(file, \"Hello, NIO.2!\", UTF_8)");
        System.out.println("      -> replaces lesson 57's ENTIRE PrintWriter/BufferedWriter/");
        System.out.println("         OutputStreamWriter/FileOutputStream stack with ONE call.");

        String readBack = Files.readString(file, StandardCharsets.UTF_8);
        System.out.println("    Files.readString(file, UTF_8)        -> \"" + readBack + "\"");

        Files.writeString(file, "line one\nline two\nline three\n");
        List<String> lines = Files.readAllLines(file);
        System.out.println("    Files.readAllLines(file)             -> " + lines);

        System.out.println();
        System.out.println("    Files.exists(file)     -> " + Files.exists(file));
        System.out.println("    Files.notExists(file)  -> " + Files.notExists(file));
        System.out.println("    Files.isDirectory(root)-> " + Files.isDirectory(root));
        System.out.println("    Files.size(file)        -> " + Files.size(file) + " bytes");

        Path copy = root.resolve("hello-copy.txt");
        Files.copy(file, copy);
        System.out.println();
        System.out.println("    Files.copy(file, copy) -> copy exists now: " + Files.exists(copy));

        Path moved = root.resolve("hello-moved.txt");
        Files.move(copy, moved);
        System.out.println("    Files.move(copy, moved)-> original copy gone: " + Files.notExists(copy)
                + ", new location exists: " + Files.exists(moved));

        Files.delete(moved);
        System.out.println("    Files.delete(moved)    -> now gone: " + Files.notExists(moved));
    }

    /* ========================================================================
     * SECTION 3 - WALKING A DIRECTORY TREE: A REAL TREE, WALKED FOR REAL
     * ======================================================================*/

    static void runDirectoryWalking(Path root) throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - Files.walk: A REAL DIRECTORY TREE");
        System.out.println("=".repeat(74));

        Path tree = root.resolve("tree");
        Files.createDirectories(tree.resolve("src/main/java"));
        Files.createDirectories(tree.resolve("src/test/java"));
        Files.createDirectories(tree.resolve("target"));
        Files.writeString(tree.resolve("src/main/java/App.java"), "class App {}");
        Files.writeString(tree.resolve("src/test/java/AppTest.java"), "class AppTest {}");
        Files.writeString(tree.resolve("README.md"), "# demo");
        Files.writeString(tree.resolve("target/App.class"), "binary junk");

        System.out.println("    built a real tree under " + tree.getFileName() + "/:");
        System.out.println("      src/main/java/App.java");
        System.out.println("      src/test/java/AppTest.java");
        System.out.println("      README.md");
        System.out.println("      target/App.class");

        System.out.println();
        System.out.println("    Files.walk() RETURNS A Stream<Path> THAT HOLDS A REAL OS DIRECTORY");
        System.out.println("    HANDLE OPEN - it MUST be closed, exactly like the FileInputStream");
        System.out.println("    in lesson 57, or that handle leaks. try-with-resources on the");
        System.out.println("    STREAM ITSELF is the correct pattern:");

        long javaFileCount;
        try (Stream<Path> walk = Files.walk(tree)) {
            javaFileCount = walk.filter(p -> p.toString().endsWith(".java")).count();
        }
        System.out.println("      try (Stream<Path> walk = Files.walk(tree)) { ... }");
        System.out.println("      .java files found -> " + javaFileCount);

        System.out.println();
        System.out.println("    FILTERING OUT target/ - a real, common need (skip build output):");
        try (Stream<Path> walk = Files.walk(tree)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> !p.toString().contains("target"))
                    .map(tree::relativize)
                    .sorted()
                    .forEach(p -> System.out.println("      " + p));
        }

        deleteRecursively(tree);
    }

    /* ========================================================================
     * SECTION 4 - FILE ATTRIBUTES: ONE OS CALL INSTEAD OF SEVERAL
     * ======================================================================*/

    static void runFileAttributes(Path root) throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - BasicFileAttributes: ONE SYSTEM CALL, MANY ANSWERS");
        System.out.println("=".repeat(74));

        Path file = root.resolve("attrs.txt");
        Files.writeString(file, "some content");

        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        System.out.println("    Files.readAttributes(file, BasicFileAttributes.class):");
        System.out.println("      size()             -> " + attrs.size() + " bytes");
        System.out.println("      isRegularFile()     -> " + attrs.isRegularFile());
        System.out.println("      isDirectory()       -> " + attrs.isDirectory());
        System.out.println("      creationTime()      -> " + attrs.creationTime());
        System.out.println();
        System.out.println("    ALL of that came from ONE underlying OS call. The old java.io.File");
        System.out.println("    API needs a SEPARATE system call per question (file.length(),");
        System.out.println("    file.lastModified(), file.isDirectory(), ...) - readAttributes");
        System.out.println("    batches them, which matters for code that checks MANY files.");

        Files.delete(file);
    }

    /* ========================================================================
     * SECTION 5 - A REAL OS-LEVEL ERROR: SYMLINK CREATION WITHOUT PRIVILEGE
     * ======================================================================*/

    static void runSymlinkPermissionReality(Path root) throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - NIO.2 SURFACES REAL OS ERRORS DIRECTLY");
        System.out.println("=".repeat(74));

        Path target = root.resolve("real-target.txt");
        Path link = root.resolve("a-symlink");
        Files.writeString(target, "the real file");

        System.out.println("    Files.createSymbolicLink(link, target) on an UNPRIVILEGED Windows");
        System.out.println("    process (no admin, no Developer Mode) genuinely fails:");
        try {
            Files.createSymbolicLink(link, target);
            System.out.println("      succeeded (this process apparently has the privilege)");
            Files.delete(link);
        } catch (FileSystemException e) {
            System.out.println("      FileSystemException: " + e.getReason());
        }
        System.out.println();
        System.out.println("    This is not a Java limitation - it is the REAL Windows");
        System.out.println("    SeCreateSymbolicLinkPrivilege requirement, surfaced faithfully by");
        System.out.println("    NIO.2 as a real, catchable, typed exception rather than silently");
        System.out.println("    failing or masking the OS's actual reason.");
        System.out.println();
        System.out.println("    THE RELATED, DOCUMENTED GOTCHA: Files.exists(path) and");
        System.out.println("    Files.notExists(path) can BOTH return false for the SAME path -");
        System.out.println("    when the check itself cannot be PERFORMED (a permission problem");
        System.out.println("    on a PARENT directory, for example), neither method can honestly");
        System.out.println("    answer yes OR no, and the JDK's own Javadoc documents exactly");
        System.out.println("    this three-way possibility. Code that assumes");
        System.out.println("    !exists(p) == notExists(p) is assuming away a REAL edge case.");

        Files.delete(target);
    }

    /* ========================================================================
     * SUPPORT
     * ======================================================================*/

    /**
     * Deletes a directory tree recursively - Files has no single "delete
     * recursively" method, deliberately, to avoid an accidental one-call
     * rm -rf. This walks bottom-up so directories are empty when deleted.
     *
     * @param path the root to delete, file or directory
     */
    static void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (DirectoryNotEmptyException | java.nio.file.NoSuchFileException e) {
                            // already handled by bottom-up order / already gone - safe to ignore here
                        } catch (IOException e) {
                            System.out.println("      could not delete " + p + ": " + e.getMessage());
                        }
                    });
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Build your own three-level directory tree with Files.createDirectories
 *    and Files.writeString, then use Files.walk with a filter to find only
 *    files LARGER than a given byte count.
 *
 * 2. Reproduce Section 3's resource-leak warning for real: call Files.walk
 *    WITHOUT try-with-resources, forget to close it, and use a tool (or
 *    just re-run this pattern 10,000 times in a loop) to observe handle
 *    exhaustion - then fix it with try-with-resources and confirm it no
 *    longer happens.
 *
 * 3. Use Files.list(dir) (NOT walk - only ONE level deep) to print just the
 *    immediate children of a directory, and explain in one sentence when
 *    you would choose list() over walk().
 *
 * 4. Write a method that copies an ENTIRE directory tree (not just one
 *    file) using Files.walk and Files.copy together - handle the fact
 *    that directories need Files.createDirectories, not Files.copy.
 *
 * 5. On a Unix-like system (or with elevated privileges/Developer Mode on
 *    Windows), actually create a symbolic link successfully, then use
 *    Files.isSymbolicLink and Files.readSymbolicLink to inspect it, and
 *    compare Files.exists(link) with Files.exists(link, LinkOption.NOFOLLOW_LINKS).
 * ============================================================================
 */
