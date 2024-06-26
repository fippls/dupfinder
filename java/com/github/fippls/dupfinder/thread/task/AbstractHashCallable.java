package com.github.fippls.dupfinder.thread.task;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 * Threaded operation for calculating a hash code.
 * @author github.com/fippls
 */
public abstract class AbstractHashCallable implements Callable<FileInfo> {
    protected final FileInfo fileInfo;

    protected AbstractHashCallable(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    /**
     * Returns a semaphore which controls the maximum number of simultaneous file operations.
     * @see Settings#maxSimultaneousFileReadsSimple For small files.
     * @see Settings#maxSimultaneousFileReadsFull For large files.
     */
    protected abstract Semaphore fileHandleSemaphore();

    /**
     * We can only have a limited number of file operations active at one time.
     */
    protected void startFileOperation() {
        try {
            fileHandleSemaphore().acquire();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.error("Interrupted while acquiring file lock: ", e.getMessage());
        }
    }

    protected void stopFileOperation() {
        fileHandleSemaphore().release();
    }

    /**
     * Fetch the total number of bytes read since last call, and reset the value.
     */
    public abstract Long getAndClearBytesRead();
}
