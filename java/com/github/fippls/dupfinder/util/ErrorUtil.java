package com.github.fippls.dupfinder.util;

import java.nio.file.AccessDeniedException;

/**
 * Error helpers.
 * @author github.com/fippls
 */
public class ErrorUtil {
    private ErrorUtil() {
        // Util class
    }

    public enum ErrorType {
        ACCESS_DENIED,
        OTHER;
    }

    public static ErrorType of(Exception e) {
        if (e instanceof AccessDeniedException) {
            return ErrorType.ACCESS_DENIED;
        }

        return ErrorType.OTHER;
    }
}
