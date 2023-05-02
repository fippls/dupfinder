package com.github.fippls.dupfinder.thread.task;

import com.github.fippls.dupfinder.detection.result.FileInfo;

/**
 * Threaded operation for calculating full MD5 hash of a file.
 * @author github.com/fippls
 */
public class FullHashCallable extends SimpleHashCallable {
    public FullHashCallable(FileInfo fileInfo) {
        super(fileInfo);
    }

    @Override
    public FileInfo call() {
        // If file is small and has already been completely hashed, just return it right away:
        if (fileInfo.isCompletelyHashed()) {
            return fileInfo;
        }

        // Large file, do full MD5 check:
        return super.call();
    }

    @Override
    protected boolean runSimpleHashCheck() {
        return false;
    }
}
