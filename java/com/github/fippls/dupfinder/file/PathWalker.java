package com.github.fippls.dupfinder.file;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.detection.result.PotentialDuplicateCollection;
import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.StringUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Grabs initial set of files from command-line arguments.
 * @author github.com/fippls
 */
public class PathWalker {
    private final List<Path> paths;

    /** The number of files that got excluded because of rules */
    private long numRuleBasedExclusions = 0;

    public PathWalker(List<Path> paths) {
        this.paths = paths;
    }

    public PotentialDuplicateCollection getApplicableFiles() {
        var fileVisitOption = Settings.followSymlinks
                ? new FileVisitOption[] { FileVisitOption.FOLLOW_LINKS }
                : new FileVisitOption[] { };

        // First step is to identify all files that have the same size
        var checkSumCollection = new PotentialDuplicateCollection("File size-based check");

        for (Path path : paths) {
            Log.info("Reading ", path, "...");

            try {
                // Walk through all files on this path and use the file size as the initial checksum
                Files.walk(path, fileVisitOption)
                        .filter(this::excludeAllBut)
                        .filter(this::hasExcludedFileEnding)
                        .filter(this::containsAnyExcludedTerm)
                        .filter(this::isReadableFile)
                        .map(FileInfo::new)
                        .filter(FileInfo::isValid)
                        .forEach(checkSumCollection::add);
            }
            catch (IOException | UncheckedIOException e) {
                Log.error("I/O exception when reading path: " + e.getMessage() + "\nPath search aborted after " +
                        checkSumCollection.numTotalFiles() + " files were scanned, continuing...");
            }
        }

        Log.info("    Input: ", checkSumCollection.numTotalFiles(), " files (", numRuleBasedExclusions,
                " excluded based on rules): ", StringUtil.getFileSizeString(checkSumCollection.totalSize()));

        checkSumCollection.resolve(true);
        return checkSumCollection;
    }

    private boolean isReadableFile(Path path) {
        var file = path.toFile();

        // This is no guarantee that we can actually read the file:
        return file.isFile() && file.canRead();
    }

    /**
     * This usually should not do anything, but in certain cases we might want to only check for certain files.
     * @param path Check if path should be included.
     * @return If {@link Settings#onlyIncludeThese} is non-empty, this filter is active.
     */
    private boolean excludeAllBut(Path path) {
        if (Settings.onlyIncludeThese.isEmpty()) {
            return true;
        }

        for (String toInclude : Settings.onlyIncludeThese) {
            if (path.toString().contains(toInclude)) {
                return true;
            }
        }

        numRuleBasedExclusions++;
        return false;
    }

    private boolean hasExcludedFileEnding(Path path) {
        for (String term : Settings.fileEndingsToExclude) {
            if (path.toString().endsWith(term)) {
                numRuleBasedExclusions++;
                return false;
            }
        }

        return true;
    }

    private boolean containsAnyExcludedTerm(Path path) {
        for (String term : Settings.stringsToExclude) {
            if (path.toString().contains(term)) {
                numRuleBasedExclusions++;
                return false;
            }
        }

        return true;
    }
}
