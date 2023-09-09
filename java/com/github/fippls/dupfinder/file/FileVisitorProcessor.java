package com.github.fippls.dupfinder.file;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.detection.result.PotentialDuplicateCollection;
import com.github.fippls.dupfinder.util.ErrorUtil;
import com.github.fippls.dupfinder.util.IntervalTimer;
import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.StringUtil;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Processes files and directories.
 * @author github.com/fippls
 */
class FileVisitorProcessor implements FileVisitor<Path> {
    private final IntervalTimer timer = new IntervalTimer(Settings.millisecondsBetweenProgressUpdates);
    private final PotentialDuplicateCollection checkSumCollection;
    /** The number of files that got excluded because of rules */
    private long numRuleBasedExclusions = 0;

    private long totalDirectoriesScanned = 0;
    private long totalFilesScanned = 0;
    private long totalFilesAdded = 0;

    FileVisitorProcessor(PotentialDuplicateCollection checkSumCollection) {
        this.checkSumCollection = checkSumCollection;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (StringUtil.containsAny(dir, Settings.directoriesToExclude)) {
            numRuleBasedExclusions++;
            return FileVisitResult.SKIP_SUBTREE;
        }

        totalDirectoriesScanned++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        if (isFileValid(path)) {
            var fileInfo = new FileInfo(path);

            if (fileInfo.isValid()) {
                checkSumCollection.add(fileInfo);
                totalFilesAdded++;
            }
        }

        totalFilesScanned++;

        if (Settings.showProgressUpdates && timer.done()) {
            Log.info("  Files added/scanned: ", totalFilesAdded, " / ", totalFilesScanned,
                    " (directories scanned: ", totalDirectoriesScanned, ")");
            timer.reset();
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exception) {
        // The "System Volume Information" in Windows somehow is never traversed as either file nor directory, but
        // still ends up causing access denied exceptions, so if that happens just don't print redundant errors:
        if (Settings.logPathErrors && !StringUtil.containsAny(path, Settings.directoriesToExclude)) {
            switch (ErrorUtil.of(exception)) {
                case ACCESS_DENIED:
                    // Don't log access denied exceptions
                    break;

                default:
                    Log.error("Error checking ", path,
                            StringUtil.skipRedundantExceptionMessage(path, exception));
                    break;
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exception) {
        return FileVisitResult.CONTINUE;
    }

    public long getNumRuleBasedExclusions() {
        return numRuleBasedExclusions;
    }

    private boolean isFileValid(Path path) {
        return excludeAllBut(path) &&
                hasExcludedFileEnding(path) &&
                containsAnyExcludedTerm(path) &&
                isReadableFile(path);
    }

    private boolean isReadableFile(Path path) {
        var file = path.toFile();

        // This is no guarantee that we can actually read the file:
        return file.isFile() && file.canRead();
    }

    /**
     * This usually should not do anything, but in certain cases we might want to only check for certain files.
     *
     * @param path Check if path should be included.
     * @return If {@link Settings#onlyIncludeThese} is non-empty, this filter is active.
     */
    private boolean excludeAllBut(Path path) {
        if (Settings.onlyIncludeThese.isEmpty()) {
            return true;
        }

        if (StringUtil.containsAny(path, Settings.onlyIncludeThese)) {
            return true;
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
        if (StringUtil.containsAny(path, Settings.stringsToExclude)) {
            numRuleBasedExclusions++;
            return false;
        }

        return true;
    }
}
