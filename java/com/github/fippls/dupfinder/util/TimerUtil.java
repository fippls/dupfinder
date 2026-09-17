package com.github.fippls.dupfinder.util;

/**
 * Util class for timer-related tasks.
 * @author github.com/fippls
 */
public class TimerUtil {
    private TimerUtil() {
        // Util class
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static double nanosToSeconds(long nanos) {
        return (double) nanos / 1.0E9D;
    }
}
