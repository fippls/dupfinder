package com.github.fippls.dupfinder.detection;

import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.thread.task.AbstractHashCallable;
import com.github.fippls.dupfinder.thread.task.FullHashCallable;

/**
 * Calculate MD5 sum of an entire file.
 * @author github.com/fippls
 */
public class FullHashChecker extends AbstractHashChecker {
    public FullHashChecker() {
        super("Full MD5 hash check", false);
    }

    @Override
    protected AbstractHashCallable createCallable(FileInfo fileInfo) {
        return new FullHashCallable(fileInfo);
    }
}
