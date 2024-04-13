package com.github.fippls.dupfinder.thread.task;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.file.MD5SumFileReader;

import java.util.concurrent.Semaphore;

/**
 * Threaded operation for calculating partial hash of a file.
 * @author github.com/fippls
 */
public class SimpleHashCallable extends AbstractHashCallable {
    private static final Semaphore fileHandleSemaphore = new Semaphore(Settings.maxSimultaneousFileReadsSimple);
    private MD5SumFileReader md5;

    public SimpleHashCallable(FileInfo fileInfo) {
        super(fileInfo);
    }

    @Override
    protected Semaphore fileHandleSemaphore() {
        return fileHandleSemaphore;
    }

    @Override
    public Long getAndClearBytesRead() {
        if (md5 == null) {
            return 0L;
        }

        return md5.getBytesReadAndReset();
    }

    @Override
    public FileInfo call() {
        startFileOperation();
        this.md5 = new MD5SumFileReader(fileInfo, runSimpleHashCheck());
        var result = md5.primeMd5();
        stopFileOperation();

        // A very minimal time is spent here compared to reading, but leave it outside the read lock anyway
        fileInfo.setHash(result);

        return fileInfo;
    }

    protected boolean runSimpleHashCheck() {
        return true;
    }
}
