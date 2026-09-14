/* ============================================================================
 * 60 - The java.time date and time API
 * ----------------------------------------------------------------------------
 * Companion lesson: 60-date-and-time-api.md
 *
 * RUN IT:
 *     java Java/11-io-files-and-time/60-date-and-time-api.java
 *
 * Section 4 reproduces a REAL Daylight Saving Time bug using an ACTUAL DST
 * transition date from the JVM's real timezone database - not a simulation.
 * ============================================================================
 */

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

class DateAndTimeApi {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE THREE "LOCAL" TYPES: NO TIMEZONE INVOLVED AT ALL
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - LocalDate, LocalTime, LocalDateTime: NO TIMEZONE, EVER");
        System.out.println("=".repeat(74));

        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalTime time = LocalTime.of(14, 30);
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        System.out.println("    LocalDate.of(2024, 3, 15)         -> " + date);
        System.out.println("    LocalTime.of(14, 30)              -> " + time);
        System.out.println("    LocalDateTime.of(date, time)      -> " + dateTime);
        System.out.println();
        System.out.println("    LocalDate.now() (WHATEVER machine runs this) -> " + LocalDate.now());
        System.out.println();
        System.out.println("    \"LOCAL\" here means EXACTLY that - a wall-clock date/time with NO");
        System.out.println("    attached timezone or offset AT ALL. \"2024-03-15T14:30\" alone does");
        System.out.println("    NOT identify one specific instant in universal time - it is a");
        System.out.println("    civil calendar reading, the same string whether you mean it in");
        System.out.println("    Tokyo or New York. Use it for BIRTHDAYS, STORE HOURS, ANYTHING");
        System.out.println("    that is genuinely tied to a WALL CLOCK, not a universal instant.");


        /* ====================================================================
         * SECTION 2 - IMMUTABLE, FOR REAL - EVERY "MUTATOR" RETURNS A NEW ONE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - EVERY java.time TYPE IS IMMUTABLE");
        System.out.println("=".repeat(74));

        LocalDate original = LocalDate.of(2024, 1, 15);
        LocalDate later = original.plusMonths(2);
        System.out.println("    original                -> " + original);
        System.out.println("    original.plusMonths(2)   -> " + later);
        System.out.println("    original UNCHANGED after -> " + original
                + "   (plusMonths RETURNED a new LocalDate, did not mutate)");

        System.out.println();
        System.out.println("    THE REAL, HISTORICAL CONTRAST - java.util.Date IS mutable, and");
        System.out.println("    that has caused REAL bugs (a Date handed to another class that");
        System.out.println("    calls .setTime() on YOUR instance corrupts state you thought was");
        System.out.println("    yours alone):");
        Date legacy = new Date(0);
        Date sameReference = legacy;
        System.out.println("      Date legacy = new Date(0)          -> " + legacy);
        sameReference.setTime(999_999_999_999L);
        System.out.println("      sameReference.setTime(...) mutates -> legacy is now " + legacy);
        System.out.println("      (the SAME object - there was never a copy to protect it)");


        /* ====================================================================
         * SECTION 3 - MONTH-OVERFLOW ARITHMETIC: CLAMPED CORRECTLY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - plusMonths ACROSS SHORTER MONTHS: MEASURED, NOT ASSUMED");
        System.out.println("=".repeat(74));

        LocalDate jan31 = LocalDate.of(2024, 1, 31);
        System.out.println("    LocalDate.of(2024, 1, 31).plusMonths(1) -> " + jan31.plusMonths(1)
                + "   (2024 is a LEAP year - Feb has 29 days)");

        LocalDate jan31nonLeap = LocalDate.of(2023, 1, 31);
        System.out.println("    LocalDate.of(2023, 1, 31).plusMonths(1) -> " + jan31nonLeap.plusMonths(1)
                + "   (2023 is NOT a leap year - Feb has 28)");
        System.out.println();
        System.out.println("    java.time CLAMPS to the target month's LAST valid day rather than");
        System.out.println("    overflowing into March, the way naive day-counting arithmetic");
        System.out.println("    might. This is correct, sensible calendar behavior, verified for");
        System.out.println("    both a leap and non-leap February above.");


        /* ====================================================================
         * SECTION 4 - THE REAL DST BUG: Duration VS Period ACROSS A REAL
         *             TRANSITION DATE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - Duration.ofDays(1) IS NOT Period.ofDays(1) - REAL PROOF");
        System.out.println("=".repeat(74));

        ZoneId newYork = ZoneId.of("America/New_York");
        // March 10, 2024 - a REAL, actual US DST transition date, 2:00 AM -> 3:00 AM,
        // taken from the ACTUAL timezone database this JVM ships with, not simulated.
        ZonedDateTime beforeTransition = ZonedDateTime.of(2024, 3, 10, 1, 30, 0, 0, newYork);
        System.out.println("    a REAL DST transition date: America/New_York, March 10 2024");
        System.out.println("    (clocks spring FORWARD from 2:00 AM to 3:00 AM - 1:30 AM to 1:59");
        System.out.println("    AM exists, but 2:00 AM to 2:59 AM does NOT exist that day)");
        System.out.println();
        System.out.println("    starting point -> " + beforeTransition);

        ZonedDateTime plusDuration = beforeTransition.plus(Duration.ofHours(24));
        ZonedDateTime plusPeriod = beforeTransition.plus(Period.ofDays(1));
        System.out.println();
        System.out.println("    .plus(Duration.ofHours(24)) -> " + plusDuration);
        System.out.println("    .plus(Period.ofDays(1))     -> " + plusPeriod);
        System.out.println();
        System.out.println("    THESE ARE DIFFERENT MOMENTS. Duration.ofHours(24) means EXACTLY");
        System.out.println("    24 real, elapsed hours - since one hour was SKIPPED that day (the");
        System.out.println("    2 AM hour never happened), 24 elapsed hours lands at 2:30 AM the");
        System.out.println("    NEXT day, not 1:30 AM. Period.ofDays(1) means \"the SAME WALL-CLOCK");
        System.out.println("    TIME, one CALENDAR day later\" - it lands at 1:30 AM the next day,");
        System.out.println("    REGARDLESS of how many hours actually elapsed.");
        System.out.println();
        System.out.println("    local time-of-day preserved? Duration -> "
                + plusDuration.toLocalTime().equals(beforeTransition.toLocalTime()));
        System.out.println("    local time-of-day preserved? Period   -> "
                + plusPeriod.toLocalTime().equals(beforeTransition.toLocalTime()));
        System.out.println();
        System.out.println("    THE RULE: Duration is for MACHINE TIME (elapsed seconds/nanos -");
        System.out.println("    timeouts, measured intervals, Instant arithmetic). Period is for");
        System.out.println("    CALENDAR TIME (\"one month from today\", \"in 2 weeks\", anything a");
        System.out.println("    HUMAN would describe in days/months/years). Using Duration for");
        System.out.println("    \"add one day\" on a ZonedDateTime is a REAL, DST-triggered bug -");
        System.out.println("    just demonstrated with an actual transition date, not a story.");


        /* ====================================================================
         * SECTION 5 - Instant: THE UNIVERSAL, TIMEZONE-FREE MACHINE TIMESTAMP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - Instant: ONE UNAMBIGUOUS POINT ON THE UNIVERSAL TIMELINE");
        System.out.println("=".repeat(74));

        Instant now = Instant.now();
        System.out.println("    Instant.now()                    -> " + now
                + "   (always UTC, always unambiguous)");

        ZonedDateTime tokyo = now.atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime losAngeles = now.atZone(ZoneId.of("America/Los_Angeles"));
        System.out.println("    the SAME instant, viewed in Tokyo -> " + tokyo);
        System.out.println("    the SAME instant, viewed in LA    -> " + losAngeles);
        System.out.println("    tokyo.toInstant().equals(losAngeles.toInstant()) -> "
                + tokyo.toInstant().equals(losAngeles.toInstant()));
        System.out.println();
        System.out.println("    TWO DIFFERENT wall-clock readings, the SAME real instant - this is");
        System.out.println("    exactly what Instant is FOR: a server timestamp, a log entry, an");
        System.out.println("    \"event happened at\" record - anything that needs to identify ONE");
        System.out.println("    real moment, unambiguously, regardless of who reads it from where.");


        /* ====================================================================
         * SECTION 6 - DateTimeFormatter: PARSING AND FORMATTING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - DateTimeFormatter: THE REPLACEMENT FOR SimpleDateFormat");
        System.out.println("=".repeat(74));

        LocalDate someDate = LocalDate.of(2024, 12, 25);
        System.out.println("    date.format(ISO_LOCAL_DATE)              -> "
                + someDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        DateTimeFormatter custom = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        System.out.println("    date.format(\"EEEE, MMMM d, yyyy\")        -> "
                + someDate.format(custom));

        LocalDate parsed = LocalDate.parse("2024-06-01");
        System.out.println("    LocalDate.parse(\"2024-06-01\")            -> " + parsed);

        DateTimeFormatter customParser = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate parsedCustom = LocalDate.parse("25/12/2024", customParser);
        System.out.println("    LocalDate.parse(\"25/12/2024\", dd/MM/yyyy)-> " + parsedCustom);
        System.out.println();
        System.out.println("    UNLIKE the old SimpleDateFormat, DateTimeFormatter is IMMUTABLE");
        System.out.println("    and THREAD-SAFE - a single formatter instance can be safely shared");
        System.out.println("    and reused across threads without external synchronization, which");
        System.out.println("    SimpleDateFormat famously could NOT do safely.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 60.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Find the FALL-BACK DST transition date for America/New_York in 2024
 *    (clocks move BACK one hour) and reproduce Section 4's experiment
 *    across THAT boundary - does Duration.ofHours(24) now land an hour
 *    EARLIER in wall-clock time instead of later? Why?
 *
 * 2. Compute your own age in YEARS, MONTHS, and DAYS (not just years) using
 *    Period.between(yourBirthDate, LocalDate.now()) - print all three
 *    fields the Period holds.
 *
 * 3. Write a method that checks whether TWO ZonedDateTime values, possibly
 *    in DIFFERENT zones, represent times within 5 minutes of EACH OTHER -
 *    using Duration, correctly, regardless of the zones involved.
 *
 * 4. Parse a date string in a completely custom format of your own design
 *    (e.g. "15th of March, 2024") using DateTimeFormatterBuilder - look up
 *    what it offers beyond ofPattern().
 *
 * 5. Convert a java.util.Date to an Instant and back (toInstant() and
 *    Date.from(instant)) and confirm the round trip preserves the exact
 *    millisecond value.
 * ============================================================================
 */
