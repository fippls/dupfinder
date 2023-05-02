package com.github.fippls.dupfinder.data;

import java.util.List;

/**
 * Global program settings.
 * @author github.com/fippls
 */
public class Settings {

    /**
     * If symbolic links should be followed or not.
     */
    public static boolean followSymlinks = false;

    /**
     * If every path-reader error should result in a message being printed.
     */
    public static boolean logPathErrors = true;

    /**
     * Enable debug logging.
     */
    public static boolean logDebugMessages = true;

    /**
     * Skip files that are smaller than this amount of bytes.
     */
    public static long minFileSize = 10_000;

    /**
     * Skip files that are larger than this amount of bytes.
     */
    public static long maxFileSize = Long.MAX_VALUE;

    /**
     * Number of bytes to read for the short MD5 check.
     * Reduction stats: Mixed-type "home directory" non-SSD storage, 1TB input, 530 GB potential duplicates, 65k files:
     *   2k check: 109 seconds 87.2% file count reduction, 7.7% size reduction
     *   20k check: 110 seconds, 88.3% file count reduction, 7.7% size reduction
     *   200k check: 111 seconds, 88.7% file count reduction, 7.7% size reduction
     *
     */
    public static int numBytesForShortMD5Check = 60_000;

    /**
     * Read buffer size for file reads.
     */
    public static int readBufferSize = numBytesForShortMD5Check * 2;

    /**
     * How often to print current task progress.
     */
    public static int millisecondsBetweenProgressUpdates = 8000;

    /**
     * Set to true to show regular progress updates.
     */
    public static boolean showProgressUpdates = true;

    /**
     * If file paths should be put in quotes.
     */
    public static boolean quotesForPaths = true;

    /**
     * Minimum number of copies before we report a duplicate.
     */
    public static int minimumCopyCount = 2;

    /**
     * The maximum number of concurrent file read operations that can be active.
     * If running via samba mounts, java can throw strange non-descriptive errors if this number is too high.
     */
    public static int maxSimultaneousFileReads = 4;

    /**
     * Number of processing threads.
     */
    public static int threadPoolSize = Math.min(
            Settings.maxSimultaneousFileReads * 2,
            Runtime.getRuntime().availableProcessors() + 1);

    /**
     * Insert this command before each file that is marked as safe to delete.
     */
    public static String automaticDeleteCommand = "del";

    /**
     * Exclude paths that contain any of the entries here.
     */
    public static final List<String> stringsToExclude = List.of();

    /**
     * Exclude all files that end with these entries.
     */
    public static final List<String> fileEndingsToExclude = List.of(".git", ".class");

    /**
     * If this list is non-empty, ONLY files containing these strings will be included for any further analysis.
     */
    public static final List<String> onlyIncludeThese = List.of();
}
