package com.github.fippls.dupfinder.thread.task;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Threaded operation for calculating a hash code.
 * @author github.com/fippls
 */
public abstract class AbstractHashCallable implements Callable<FileInfo> {
    private static final Semaphore fileHandles = new Semaphore(Settings.maxSimultaneousFileReads);
    /**
     * Every task will add the final processed size to this queue.
     * The statistics thread will empty it out and summarize.
     */
    protected static final Queue<Long> numBytesProcessed = new LinkedList<>();
    protected final FileInfo fileInfo;

    protected AbstractHashCallable(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    /**
     * We can only have a limited number of file operations active at one time.
     */
    protected void startFileOperation() {
        try {
            fileHandles.acquire();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.error("Interrupted while acquiring file lock: " + e.getMessage());
        }
    }

    protected void stopFileOperation() {
        fileHandles.release();
    }

    /**
     * Fetch the total number of bytes read since last call, and empty the queue of read stats.
     */
    public static Long getAndClearBytesRead() {
        final var total = new AtomicLong(0);
        synchronized (numBytesProcessed) {
            numBytesProcessed
                    .forEach(total::addAndGet);

            numBytesProcessed.clear();
        }

        return total.get();
    }
}
