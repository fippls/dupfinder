package com.github.fippls.dupfinder.thread.task;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.file.MD5SumFileReader;

/**
 * Threaded operation for calculating partial hash of a file.
 * @author github.com/fippls
 */
public class SimpleHashCallable extends AbstractHashCallable {

    public SimpleHashCallable(FileInfo fileInfo) {
        super(fileInfo);
    }

    @Override
    public FileInfo call() {
        startFileOperation();
        MD5SumFileReader md5 = new MD5SumFileReader(fileInfo, runSimpleHashCheck());
        var result = md5.primeMd5();
        stopFileOperation();

        fileInfo.setHash(result);

        synchronized (numBytesProcessed) {
            var fileSize = runSimpleHashCheck()
                    ? Settings.numBytesForShortMD5Check
                    : fileInfo.fileSize();

            numBytesProcessed.add(fileSize);
        }

        return fileInfo;
    }

    protected boolean runSimpleHashCheck() {
        return true;
    }
}
