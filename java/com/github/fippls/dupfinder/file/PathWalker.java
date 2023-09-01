package com.github.fippls.dupfinder.file;

import com.github.fippls.dupfinder.detection.result.PotentialDuplicateCollection;
import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.StringUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Grabs initial set of files from command-line arguments.
 * @author github.com/fippls
 */
public class PathWalker {
    private final List<Path> paths;

    public PathWalker(List<Path> paths) {
        this.paths = paths;
    }

    public PotentialDuplicateCollection getApplicableFiles() {
        // First step is to identify all files that have the same size
        var checkSumCollection = new PotentialDuplicateCollection("File size-based check");
        var fileProcessor = new FileVisitorProcessor(checkSumCollection);

        for (Path path : paths) {
            Log.info("Reading ", path, "...");

            try {
                // Walk through all files on this path and use the file size as the initial checksum
                Files.walkFileTree(path, fileProcessor);
            }
            catch (IOException | UncheckedIOException e) {
                Log.error("I/O exception when reading path: " + e.getMessage() + "\nPath search aborted after " +
                        checkSumCollection.numTotalFiles() + " files were scanned, continuing...");
            }
        }

        Log.info("    Input: ", checkSumCollection.numTotalFiles(), " files (",
                fileProcessor.getNumRuleBasedExclusions(), " excluded based on rules): ",
                StringUtil.getFileSizeString(checkSumCollection.totalSize()));

        checkSumCollection.resolve(true);
        return checkSumCollection;
    }
}
