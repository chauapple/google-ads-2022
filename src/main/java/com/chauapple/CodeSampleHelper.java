package com.chauapple;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class CodeSampleHelper {

    /**
     * The date format used for printing.
     */
    private static final DateTimeFormatter format =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    /**
     * The shorter date format used for printing.
     */
    private static final DateTimeFormatter shortFormat =
            DateTimeFormatter.ofPattern("MMddHHmmssSSS");

    /**
     * Generates a printable string for the current date and time in local time zone.
     *
     * @return the result string.
     */
    public static String getPrintableDateTime() {
        return ZonedDateTime.now().format(format);
    }

    /**
     * Generates a short printable string for the current date and time in local time zone.
     *
     * @return the result string.
     */
    public static String getShortPrintableDateTime() {
        return ZonedDateTime.now().format(shortFormat);
    }
}
