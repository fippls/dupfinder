package com.github.fippls.dupfinder.util;

import com.github.fippls.dupfinder.data.Settings;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * Super-primitive logger.
 * @author github.com/fippls
 */
public class Log {
    private Log() {
        // Not allowed
    }

    public static void debug(Object... messages) {
        if (Settings.logDebugMessages) {
            print(System.out, messages);
        }
    }

    public static void info(Object... messages) {
        print(System.out, messages);
    }

    public static void error(Object... messages) {
        print(System.err, messages);
    }

    public static void pathError(Object... messages) {
        if (Settings.logPathErrors) {
            print(System.err, messages);
        }
    }

    private static void print(PrintStream stream, Object... messages) {
        Arrays.stream(messages).forEach(stream::print);
        stream.println();
    }
}
