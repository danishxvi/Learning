/* ============================================================================
 * 57 - File I/O with streams and readers
 * ----------------------------------------------------------------------------
 * Companion lesson: 57-file-io-basics.md
 *
 * RUN IT:
 *     java Java/11-io-files-and-time/57-file-io-basics.java
 *
 * This lesson touches the REAL file system - every file it creates is a real
 * temp file, and every one is deleted at the end. Section 2 reproduces a
 * real, classic encoding bug. Section 3 measures buffering's real cost.
 * ============================================================================
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

class FileIoBasics {

    public static void main(String[] args) throws IOException {

        File byteFile = File.createTempFile("lesson57-bytes", ".bin");
        File charFile = File.createTempFile("lesson57-chars", ".txt");
        File encodingFile = File.createTempFile("lesson57-encoding", ".txt");
        File bigFile = File.createTempFile("lesson57-big", ".txt");
        File printFile = File.createTempFile("lesson57-print", ".txt");

        try {
            runByteStreams(byteFile);
            runCharacterStreamsAndEncoding(charFile, encodingFile);
            runBuffering(bigFile);
            runPrintWriter(printFile);
        } finally {
            byteFile.delete();
            charFile.delete();
            encodingFile.delete();
            bigFile.delete();
            printFile.delete();
        }

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 57. All 5 temp files created above were deleted.");
        System.out.println("=".repeat(74));
    }

    /* ========================================================================
     * SECTION 1 - BYTE STREAMS: RAW BYTES, NO ENCODING INVOLVED AT ALL
     * ======================================================================*/

    static void runByteStreams(File file) throws IOException {
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - InputStream/OutputStream: RAW BYTES");
        System.out.println("=".repeat(74));

        System.out.println("    writing to a REAL temp file: " + file.getName());
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(65);              // ONE byte - 'A'
            out.write(new byte[]{66, 67, 68});   // 'B', 'C', 'D'
        }

        try (FileInputStream in = new FileInputStream(file)) {
            System.out.print("    reading it back, byte by byte: ");
            int b;
            while ((b = in.read()) != -1) {
                System.out.print((char) b);
            }
            System.out.println();
        }

        System.out.println("    file.length() on disk -> " + file.length() + " bytes"
                + "   (exactly 4 - ONE byte per ASCII character, no encoding overhead)");
        System.out.println();
        System.out.println("    InputStream/OutputStream work in raw BYTES - they know NOTHING");
        System.out.println("    about characters, text, or encoding. Writing text through them");
        System.out.println("    means encoding it to bytes YOURSELF (String.getBytes(charset)).");
    }

    /* ========================================================================
     * SECTION 2 - CHARACTER STREAMS, AND THE REAL DEFAULT-ENCODING BUG
     * ======================================================================*/

    static void runCharacterStreamsAndEncoding(File charFile, File encodingFile) throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - Reader/Writer: CHARACTERS, WHICH MEANS AN ENCODING");
        System.out.println("=".repeat(74));

        try (FileWriter writer = new FileWriter(charFile)) {
            writer.write("Hello, World!");
        }
        try (FileReader reader = new FileReader(charFile);
             BufferedReader buffered = new BufferedReader(reader)) {
            System.out.println("    FileWriter -> FileReader round trip -> \"" + buffered.readLine() + "\"");
        }
        System.out.println();
        System.out.println("    THE REAL PROBLEM: FileWriter/FileReader use");
        System.out.println("    Charset.defaultCharset() -> " + java.nio.charset.Charset.defaultCharset()
                + " on THIS machine - SILENTLY, with NO way to override it on");
        System.out.println("    FileWriter/FileReader themselves. A different machine, a");
        System.out.println("    different default JVM charset (this varies by OS and locale), and");
        System.out.println("    the SAME code can read back DIFFERENT characters for the same file.");

        System.out.println();
        System.out.println("    REPRODUCING A REAL MOJIBAKE BUG - writing text containing a");
        System.out.println("    character outside ASCII with ONE encoding, reading it back with a");
        System.out.println("    DIFFERENT one. Verified by CODEPOINT, not by how it happens to");
        System.out.println("    LOOK on any one terminal - a terminal's own encoding is a THIRD,");
        System.out.println("    independent variable that has nothing to do with whether the read");
        System.out.println("    actually matched the write, and can make correct output look wrong");
        System.out.println("    (or corrupted output look right) depending on how IT decodes bytes:");

        String original = "café";
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(encodingFile), StandardCharsets.UTF_8)) {
            writer.write(original);
        }
        System.out.println("      original: length=" + original.length()
                + ", codepoints=" + codepoints(original));

        String wrongReadBack;
        try (Reader wrongReader = new InputStreamReader(
                new FileInputStream(encodingFile), StandardCharsets.ISO_8859_1);
             BufferedReader buffered = new BufferedReader(wrongReader)) {
            wrongReadBack = buffered.readLine();
        }
        System.out.println("      read as ISO-8859-1 (WRONG): length=" + wrongReadBack.length()
                + ", codepoints=" + codepoints(wrongReadBack)
                + ", equals original? " + wrongReadBack.equals(original));

        String rightReadBack;
        try (Reader rightReader = new InputStreamReader(
                new FileInputStream(encodingFile), StandardCharsets.UTF_8);
             BufferedReader buffered = new BufferedReader(rightReader)) {
            rightReadBack = buffered.readLine();
        }
        System.out.println("      read as UTF-8       (right): length=" + rightReadBack.length()
                + ", codepoints=" + codepoints(rightReadBack)
                + ", equals original? " + rightReadBack.equals(original));
        System.out.println();
        System.out.println("    The wrong-charset read came back as a DIFFERENT LENGTH (5 chars,");
        System.out.println("    not 4) and DIFFERENT codepoints - a single 2-byte UTF-8 character");
        System.out.println("    (U+00E9) was misread as TWO separate Latin-1 characters (U+00C3,");
        System.out.println("    U+00A9). That mismatch is real and unambiguous, independent of how");
        System.out.println("    any particular terminal chooses to DISPLAY either string.");
        System.out.println();
        System.out.println("    THE FIX: never rely on the platform default. Use");
        System.out.println("    InputStreamReader/OutputStreamWriter with an EXPLICIT");
        System.out.println("    java.nio.charset.StandardCharsets constant (almost always UTF_8),");
        System.out.println("    not the bare FileReader/FileWriter constructors.");
    }

    /* ========================================================================
     * SECTION 3 - BUFFERING: MEASURED, NOT ASSERTED
     * ======================================================================*/

    static void runBuffering(File file) throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - BufferedReader/BufferedWriter: THE REAL COST OF SKIPPING THEM");
        System.out.println("=".repeat(74));

        int lines = 50_000;
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (int i = 0; i < lines; i++) {
                writer.println("line number " + i + " of the generated file");
            }
        }
        System.out.println("    generated a real " + file.length() + "-byte file, " + lines + " lines");

        long unbufferedStart = System.nanoTime();
        int unbufferedCount = 0;
        try (FileReader reader = new FileReader(file)) {
            int c;
            while ((c = reader.read()) != -1) {   // ONE CHARACTER AT A TIME, no buffer
                if (c == '\n') unbufferedCount++;
            }
        }
        long unbufferedMillis = (System.nanoTime() - unbufferedStart) / 1_000_000;

        long bufferedStart = System.nanoTime();
        int bufferedCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bufferedCount++;
            }
        }
        long bufferedMillis = (System.nanoTime() - bufferedStart) / 1_000_000;

        System.out.println();
        System.out.println("    reading " + lines + " lines, UNBUFFERED (read() one char at a time)"
                + " -> " + unbufferedMillis + " ms");
        System.out.println("    reading " + lines + " lines, BUFFERED (BufferedReader.readLine())"
                + "   -> " + bufferedMillis + " ms");
        System.out.println("    (line counts match: " + (unbufferedCount == bufferedCount) + ")");
        System.out.println();
        System.out.println("    EVERY unbuffered reader.read() call is a POTENTIAL real disk/OS");
        System.out.println("    read - the OS and FileReader do some of their own buffering, but");
        System.out.println("    crossing the Java-to-native boundary ONE CHARACTER AT A TIME still");
        System.out.println("    costs real, measurable overhead per call. BufferedReader reads a");
        System.out.println("    large CHUNK into memory ONCE, then serves reads from THAT chunk -");
        System.out.println("    always wrap a raw FileReader/FileWriter in a Buffered- version for");
        System.out.println("    anything beyond a trivial, tiny file.");
    }

    /* ========================================================================
     * SECTION 4 - PrintWriter: THE CONVENIENT WAY TO WRITE TEXT
     * ======================================================================*/

    static void runPrintWriter(File file) throws IOException {
        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - PrintWriter: println/printf ON TOP OF ANY Writer");
        System.out.println("=".repeat(74));

        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))) {
            writer.println("plain line");
            writer.printf("formatted: %d items, %.2f%% done%n", 42, 87.5);
            writer.write("write() still works too, no newline");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            System.out.println("    written with PrintWriter.println/printf/write, read back:");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("      " + line);
            }
        }
        System.out.println();
        System.out.println("    PrintWriter wraps ANY Writer and adds println/print/printf on top -");
        System.out.println("    this exact stack (PrintWriter -> BufferedWriter -> OutputStreamWriter");
        System.out.println("    -> FileOutputStream, with an EXPLICIT charset) is the standard,");
        System.out.println("    correct way to write text to a file in real code, BEFORE lesson 58");
        System.out.println("    shows the NIO.2 Files.newBufferedWriter shortcut for the same thing.");
    }

    /** @param s a string @return its characters as a bracketed list of U+XXXX codepoints */
    static String codepoints(String s) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < s.length(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("U+").append(String.format("%04X", (int) s.charAt(i)));
        }
        return sb.append("]").toString();
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Reproduce Section 2's mojibake bug with a DIFFERENT character (try an
 *    emoji, which needs 4 UTF-8 bytes) and a DIFFERENT wrong charset.
 *
 * 2. Section 3 benchmarks read() vs readLine(). Add a THIRD measurement:
 *    FileReader wrapped directly in a char[] buffer you manage yourself
 *    (reader.read(char[], int, int)) and compare all three.
 *
 * 3. Write a method that copies one file to another using ONLY byte streams
 *    (FileInputStream/FileOutputStream, no character conversion at all) -
 *    confirm it works correctly even on a file containing text your
 *    default charset cannot represent, since bytes are never interpreted.
 *
 * 4. Deliberately throw an exception INSIDE a try-with-resources block
 *    that has TWO resources, and confirm (via printlns in a custom
 *    AutoCloseable) that BOTH still get closed, in REVERSE order of
 *    declaration - lesson 41 covered this promise; verify it for real
 *    with file resources specifically.
 *
 * 5. Time appending 10,000 lines to a file with FileWriter(file, true) one
 *    line at a time (opening and closing the stream EVERY line) versus
 *    holding ONE stream open for all 10,000 - explain the gap.
 * ============================================================================
 */
