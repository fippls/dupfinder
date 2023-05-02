package com.github.fippls.dupfinder.detection;

import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.thread.task.AbstractHashCallable;
import com.github.fippls.dupfinder.thread.task.SimpleHashCallable;

/**
 * Calculate MD5 for parts of a file (or the full file if it's smaller than the hash size).
 * @see com.github.fippls.dupfinder.data.Settings#numBytesForShortMD5Check Hash size used by this class.
 * @author github.com/fippls
 */
public class SimpleHashChecker extends AbstractHashChecker {
    public SimpleHashChecker() {
        super("Simple MD5 hash check", true);
    }

    @Override
    protected AbstractHashCallable createCallable(FileInfo fileInfo) {
        return new SimpleHashCallable(fileInfo);
    }
}
