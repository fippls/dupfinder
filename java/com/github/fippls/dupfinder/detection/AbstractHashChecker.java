package com.github.fippls.dupfinder.detection;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.detection.result.PotentialDuplicateCollection;
import com.github.fippls.dupfinder.thread.ThreadPool;
import com.github.fippls.dupfinder.thread.task.AbstractHashCallable;
import com.github.fippls.dupfinder.util.IntervalTimer;
import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.StringUtil;

/**
 * Abstract implementation of something that calculates hash codes for files.
 * @author github.com/fippls
 */
public abstract class AbstractHashChecker {
    private static final ThreadPool THREAD_POOL = new ThreadPool();
    /** Display stats for file count/size reduction, not needed when final step is completed */
    protected final boolean optimizationStats;
    private final String name;
    private final PotentialDuplicateCollection potentialDuplicateCollection;

    protected AbstractHashChecker(String name, boolean optimizationStats) {
        this.name = name;
        this.optimizationStats = optimizationStats;
        potentialDuplicateCollection = new PotentialDuplicateCollection(name);
    }

    public PotentialDuplicateCollection scan(PotentialDuplicateCollection potentialDuplicates) {
        Log.info(name, " received ", potentialDuplicates.numTotalFiles(), " files to process: ",
                StringUtil.getFileSizeString(potentialDuplicates.totalSize()));

        potentialDuplicates
                .mapAllFiles(this::createCallable)
                .forEach(THREAD_POOL::addTask);

        final IntervalTimer timer = new IntervalTimer(Settings.millisecondsBetweenProgressUpdates);
        final Runtime runtime = Runtime.getRuntime();
        boolean finished = false;

        while (!finished) {
            long numDone = THREAD_POOL.numDone();

            if (Settings.showProgressUpdates && timer.done()) {
                Log.info("  Task ", numDone, " / ", THREAD_POOL.getNumTasks(),
                        " (free memory: ", StringUtil.getFileSizeString(runtime.freeMemory()), ')');
                timer.reset();
            }

            finished = numDone >= THREAD_POOL.getNumTasks();

            if (!finished) {
                sleep(100);
            }
        }

        THREAD_POOL.fetchResult()
                .forEach(potentialDuplicateCollection::add);

        return potentialDuplicateCollection.resolve(optimizationStats);
    }

    protected abstract AbstractHashCallable createCallable(FileInfo fileInfo);

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
