package com.github.fippls.dupfinder.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Utilities for file paths.
 * @author github.com/fippls
 */
public class PathUtil {
    /** Returned when the size of a file can't be determined */
    public static final long SIZE_ERROR = -1;

    private PathUtil() {
        // Not allowed
    }

    /**
     * Converts a path string to an optional path object.
     */
    public static Optional<Path> of(String path) {
        try {
            return Optional.of(Paths.get(path));
        }
        catch (InvalidPathException e) {
            Log.pathError("Unable to read path ", path, ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetches the size of a file.
     * @return File size, or {@link PathUtil#SIZE_ERROR} on error.
     */
    public static long getFileSize(Path path) {
        try {
            return Files.size(path);
        }
        catch (IOException e) {
            Log.pathError("Unable to fetch file size for path ", path, ": ", e.getMessage());
            return SIZE_ERROR;
        }
    }
}