package com.github.fippls.dupfinder;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.FullHashChecker;
import com.github.fippls.dupfinder.detection.SimpleHashChecker;
import com.github.fippls.dupfinder.detection.output.FileSizeBasedDuplicationPrinter;
import com.github.fippls.dupfinder.file.PathWalker;
import com.github.fippls.dupfinder.thread.ThreadPool;
import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.PathUtil;
import com.github.fippls.dupfinder.util.PerformanceTimer;
import com.github.fippls.dupfinder.util.StringUtil;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * File duplication finder.
 *
 * TODO: Parameters: --minsize X, --maxsize X
 *
 * @author github.com/fippls
 */
public class DupFinder {
    private static final String VERSION = "1.0";

    public static void main(String[] args) {
        System.out.println(version() + " (using " + Settings.threadPoolSize + " threads)");
        PerformanceTimer totalTime = new PerformanceTimer();

        if (args.length == 0) {
            System.out.println("Usage: " + DupFinder.class.getSimpleName() + " PATH1 {PATH2} {PATH3...}");
            System.out.println("   Searches through all given paths, finding duplicated files and listing these");
            System.exit(1);
        }

        var initialPaths = Arrays.stream(args)
                .map(PathUtil::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // Step 1: Identify all files that share the same file size (otherwise can't possible be duplicate)
        var potentialDuplicates = new PathWalker(initialPaths).getApplicableFiles();

        // Step 2: Run a quick MD5 scan on the files above to see if there are any potential duplicates to leave out:
        potentialDuplicates = new SimpleHashChecker().scan(potentialDuplicates);

        // Step 3: Run a full MD5 scan on all remaining files to identify the actual duplicates:
        var duplicates = new FullHashChecker().scan(potentialDuplicates);

        totalTime.stop();
        ThreadPool.shutDown();

        var duplicationPrinter = new FileSizeBasedDuplicationPrinter();
        Log.info("\nDone after ", StringUtil.doubleToString1Decimal(totalTime.seconds()),
                " seconds, found ", duplicates.numDuplicatedFiles() + " duplicated files (",
                StringUtil.getFileSizeString(duplicates.totalDuplicatedSize()), ')');

        duplicationPrinter.printDuplicates(duplicates);
    }

    private static String version() {
        return "Fippls industries DupFinder v." + VERSION;
    }
}
